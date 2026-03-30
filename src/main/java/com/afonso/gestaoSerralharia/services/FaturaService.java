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
        return faturaRepository.findByIdObra(obra);
    }

    /**
     * Emite uma fatura para uma obra a partir do orçamento aprovado.
     * O valor total é calculado automaticamente pelas linhas do orçamento.
     */
    public Fatura emitir(Obra obra) {
        if (obra == null)
            throw new IllegalArgumentException("A obra é obrigatória.");

        if (!faturaRepository.findByIdObra(obra).isEmpty())
            throw new IllegalStateException("Esta obra já tem uma fatura emitida.");

        Orcamento orcamento = orcamentoRepository.findByIdObra(obra)
                .orElseThrow(() -> new IllegalStateException(
                        "Esta obra não tem orçamento. Cria e aprova um orçamento primeiro."));

        if (!orcamento.getAprovado())
            throw new IllegalStateException(
                    "O orçamento desta obra ainda não foi aprovado. Aprova-o antes de emitir a fatura.");

        List<Linhaorcamento> linhas = linhaorcamentoRepository.findByIdOrcamento(orcamento);
        if (linhas.isEmpty())
            throw new IllegalStateException("O orçamento não tem linhas. Adiciona artigos antes de faturar.");

        BigDecimal totalComIva = linhas.stream()
                .map(l -> {
                    BigDecimal subtotal = l.getPrecoUnit().multiply(l.getQuantidade());
                    BigDecimal iva = l.getIvaPercentagemAplicada()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    return subtotal.multiply(BigDecimal.ONE.add(iva));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Fatura fatura = new Fatura();
        fatura.setIdObra(obra);
        fatura.setValorTotalComIva(totalComIva);
        fatura.setValorPago(BigDecimal.ZERO);
        fatura.setDataEmissao(LocalDate.now());

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

    public void eliminar(Integer id) {
        Fatura fatura = buscarPorId(id);
        if (fatura.getValorPago().compareTo(BigDecimal.ZERO) > 0)
            throw new IllegalStateException("Não é possível eliminar uma fatura com pagamentos registados.");
        faturaRepository.deleteById(id);
    }
}