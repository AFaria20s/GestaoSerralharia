package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EstadopagamentoRepository;
import com.afonso.gestaoSerralharia.repositories.FaturaRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaRepository          faturaRepository;
    private final OrcamentoRepository       orcamentoRepository;
    private final LinhaorcamentoRepository  linhaorcamentoRepository;
    private final EstadopagamentoRepository estadopagamentoRepository;

    public List<Fatura> listarTodos() {
        return faturaRepository.findAll();
    }

    public Fatura buscarPorId(Integer id) {
        return faturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
    }

    public List<Fatura> buscarPorObra(Obra obra) {
        return faturaRepository.findByIdObraOrderByNumeroParcelaAsc(obra);
    }

    /**
     * Emite uma fatura para uma obra a partir do orçamento aprovado.
     * O valor total é calculado automaticamente pelas linhas do orçamento.
     */
    public Fatura emitir(Obra obra) {
        Orcamento orcamento = orcamentoAprovado(obra);
        BigDecimal restante = saldoPorFaturar(obra, orcamento);
        return emitir(obra, restante, "Faturação total");
    }

    public Fatura emitir(Obra obra, BigDecimal valor, String descricao) {
        if (obra == null)
            throw new IllegalArgumentException("A obra é obrigatória.");

        Orcamento orcamento = orcamentoAprovado(obra);
        List<Linhaorcamento> linhas = linhaorcamentoRepository.findByIdOrcamento(orcamento);
        if (linhas.isEmpty())
            throw new IllegalStateException("O orçamento não tem linhas. Adiciona artigos antes de faturar.");

        BigDecimal restante = saldoPorFaturar(obra, orcamento);
        if (restante.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("O valor do orçamento aprovado já foi totalmente faturado.");
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException("O valor da fatura tem de ser maior que zero.");
        if (valor.compareTo(restante) > 0)
            throw new IllegalArgumentException("O valor da fatura não pode exceder o valor ainda por faturar.");

        Fatura fatura = new Fatura();
        fatura.setIdObra(obra);
        fatura.setIdOrcamento(orcamento);
        fatura.setValorTotalComIva(valor.setScale(2, RoundingMode.HALF_UP));
        fatura.setValorPago(BigDecimal.ZERO);
        fatura.setDataEmissao(LocalDate.now());
        fatura.setNumeroParcela(buscarPorObra(obra).size() + 1);
        fatura.setDescricao(descricao == null || descricao.isBlank()
                ? "Parcela " + fatura.getNumeroParcela()
                : descricao.trim());

        estadopagamentoRepository.findByNomeEstadoIgnoreCase("Pendente")
                .ifPresent(fatura::setIdEstadoPagamento);

        return faturaRepository.save(fatura);
    }

    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago) {
        Fatura fatura = buscarPorId(idFatura);

        if (valorPago == null || valorPago.signum() <= 0)
            throw new IllegalArgumentException("O valor pago tem de ser maior que zero.");

        BigDecimal saldo = fatura.getValorTotalComIva().subtract(fatura.getValorPago());
        if (valorPago.compareTo(saldo) > 0)
            throw new IllegalArgumentException(
                    String.format("O valor pago (%.2f €) não pode exceder o saldo em dívida (%.2f €).",
                            valorPago, saldo));

        fatura.setValorPago(fatura.getValorPago().add(valorPago));

        if (fatura.getValorPago().compareTo(fatura.getValorTotalComIva()) >= 0)
            estadopagamentoRepository.findByNomeEstadoIgnoreCase("Pago")
                    .ifPresent(fatura::setIdEstadoPagamento);
        else
            estadopagamentoRepository.findByNomeEstadoIgnoreCase("Parcial")
                    .ifPresent(fatura::setIdEstadoPagamento);

        return faturaRepository.save(fatura);
    }

    public BigDecimal totalFaturado(Obra obra) {
        return buscarPorObra(obra).stream()
                .map(Fatura::getValorTotalComIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal saldoPorFaturar(Obra obra, Orcamento orcamento) {
        BigDecimal totalOrcamento = totalOrcamentoComIva(orcamento);
        BigDecimal totalJaFaturado = totalFaturado(obra);
        BigDecimal saldo = totalOrcamento.subtract(totalJaFaturado);
        return saldo.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public void eliminar(Integer id) {
        Fatura fatura = buscarPorId(id);
        if (fatura.getValorPago().compareTo(BigDecimal.ZERO) > 0)
            throw new IllegalStateException("Não é possível eliminar uma fatura com pagamentos registados.");
        faturaRepository.deleteById(id);
    }

    private Orcamento orcamentoAprovado(Obra obra) {
        Orcamento orcamento = orcamentoRepository.findFirstByIdObraAndAprovadoTrueOrderByVersaoDesc(obra)
                .orElseThrow(() -> new IllegalStateException(
                        "Esta obra não tem um orçamento aprovado. Aprova um orçamento antes de faturar."));
        if (!Boolean.TRUE.equals(orcamento.getAprovado()))
            throw new IllegalStateException("O orçamento desta obra ainda não foi aprovado.");
        return orcamento;
    }

    private BigDecimal totalOrcamentoComIva(Orcamento orcamento) {
        return linhaorcamentoRepository.findByIdOrcamento(orcamento).stream()
                .map(l -> {
                    BigDecimal subtotal = l.getPrecoUnit().multiply(l.getQuantidade());
                    BigDecimal iva = l.getIvaPercentagemAplicada()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    return subtotal.multiply(BigDecimal.ONE.add(iva));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
