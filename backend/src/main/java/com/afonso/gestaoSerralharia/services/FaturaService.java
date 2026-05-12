package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.util.CalculosFinanceiros;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.FaturaResumoIva;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.models.PagamentoFatura;
import com.afonso.gestaoSerralharia.repositories.EstadopagamentoRepository;
import com.afonso.gestaoSerralharia.repositories.FaturaRepository;
import com.afonso.gestaoSerralharia.repositories.FaturaResumoIvaRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.PagamentoFaturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private static final String ESTADO_PENDENTE = "Pendente";
    private static final String ESTADO_PARCIAL = "Parcial";
    private static final String ESTADO_PAGO = "Pago";
    private static final String MEIO_PAGAMENTO_PADRAO = "Transferência Bancária";

    private final FaturaRepository faturaRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final LinhaorcamentoRepository linhaorcamentoRepository;
    private final EstadopagamentoRepository estadopagamentoRepository;
    private final FaturaResumoIvaRepository faturaResumoIvaRepository;
    private final PagamentoFaturaRepository pagamentoFaturaRepository;

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

    public List<PagamentoFatura> listarPagamentos(Fatura fatura) {
        migrarPagamentosAgregadosSeNecessario(fatura);
        return pagamentoFaturaRepository.findByIdFaturaOrderByDataPagamentoAscIdAsc(fatura);
    }

    public List<ResumoIva> listarResumoIva(Fatura fatura) {
        List<FaturaResumoIva> guardados = faturaResumoIvaRepository.findByIdFaturaOrderByTaxaPercentagemAsc(fatura);
        if (!guardados.isEmpty()) {
            return guardados.stream()
                    .map(r -> new ResumoIva(
                            CalculosFinanceiros.normalizarTaxa(r.getTaxaPercentagem()),
                            CalculosFinanceiros.normalizarDinheiro(r.getValorBase()),
                            CalculosFinanceiros.normalizarDinheiro(r.getValorIva()),
                            CalculosFinanceiros.normalizarDinheiro(r.getValorTotal())))
                    .toList();
        }

        if (fatura.getIdOrcamento() == null) {
            BigDecimal total = CalculosFinanceiros.normalizarDinheiro(fatura.getValorTotalComIva());
            return List.of(new ResumoIva(BigDecimal.ZERO, total, BigDecimal.ZERO, total));
        }

        List<ResumoIva> resumoOrcamento = CalculosFinanceiros.resumoIva(
                linhaorcamentoRepository.findByIdOrcamento(fatura.getIdOrcamento()));
        return alocarResumoIva(resumoOrcamento, CalculosFinanceiros.normalizarDinheiro(fatura.getValorTotalComIva()));
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
        BigDecimal valorNormalizado = CalculosFinanceiros.normalizarDinheiro(valor);
        if (restante.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("O valor do orçamento aprovado já foi totalmente faturado.");
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException("O valor da fatura tem de ser maior que zero.");
        if (valorNormalizado.compareTo(restante) > 0)
            throw new IllegalArgumentException("O valor da fatura não pode exceder o valor ainda por faturar.");

        List<ResumoIva> resumoSaldo = saldoIvaPorFaturar(obra, orcamento);
        List<ResumoIva> resumoEmitido = alocarResumoIva(resumoSaldo, valorNormalizado);

        Fatura fatura = new Fatura();
        fatura.setIdObra(obra);
        fatura.setIdOrcamento(orcamento);
        fatura.setValorTotalComIva(valorNormalizado);
        fatura.setValorSubtotalSemIva(somarBase(resumoEmitido));
        fatura.setValorIva(somarIva(resumoEmitido));
        fatura.setValorPago(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        fatura.setDataEmissao(LocalDate.now());
        fatura.setDataVencimento(fatura.getDataEmissao().plusDays(prazoPagamento(orcamento)));
        fatura.setNumeroParcela(buscarPorObra(obra).size() + 1);
        fatura.setDescricao(descricao == null || descricao.isBlank()
                ? "Parcela " + fatura.getNumeroParcela()
                : descricao.trim());
        fatura.setObservacoesPagamento(orcamento.getObservacoesFinanceiras());
        preencherSnapshotCliente(fatura, obra.getIdCliente(), obra);

        estadopagamentoRepository.findByNomeEstadoIgnoreCase(ESTADO_PENDENTE)
                .ifPresent(fatura::setIdEstadoPagamento);

        Fatura guardada = faturaRepository.save(fatura);
        guardada.setCodigoDocumento(codigoDocumento(guardada));
        guardada = faturaRepository.save(guardada);

        guardarResumoIva(guardada, resumoEmitido);
        return guardada;
    }

    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago) {
        return registarPagamento(idFatura, valorPago, LocalDate.now(), MEIO_PAGAMENTO_PADRAO, null, null);
    }

    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago, LocalDate dataPagamento,
                                    String meioPagamento, String referencia, String observacoes) {
        Fatura fatura = buscarPorId(idFatura);
        migrarPagamentosAgregadosSeNecessario(fatura);

        BigDecimal valorNormalizado = CalculosFinanceiros.normalizarDinheiro(valorPago);
        if (valorPago == null || valorNormalizado.signum() <= 0)
            throw new IllegalArgumentException("O valor pago tem de ser maior que zero.");
        if (dataPagamento == null)
            throw new IllegalArgumentException("A data de pagamento é obrigatória.");
        if (meioPagamento == null || meioPagamento.isBlank())
            throw new IllegalArgumentException("O meio de pagamento é obrigatório.");

        BigDecimal saldo = saldoEmDivida(fatura);
        if (valorNormalizado.compareTo(saldo) > 0)
            throw new IllegalArgumentException(
                    String.format("O valor pago (%.2f €) não pode exceder o saldo em dívida (%.2f €).",
                            valorNormalizado, saldo));

        PagamentoFatura pagamento = new PagamentoFatura();
        pagamento.setIdFatura(fatura);
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setValorPago(valorNormalizado);
        pagamento.setMeioPagamento(meioPagamento.trim());
        pagamento.setReferenciaPagamento(referencia != null && !referencia.isBlank() ? referencia.trim() : null);
        pagamento.setObservacoes(observacoes != null && !observacoes.isBlank() ? observacoes.trim() : null);
        pagamentoFaturaRepository.save(pagamento);

        return recalcularPagamentosEEstado(fatura);
    }

    public BigDecimal totalFaturado(Obra obra) {
        return buscarPorObra(obra).stream()
                .map(Fatura::getValorTotalComIva)
                .map(CalculosFinanceiros::normalizarDinheiro)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal saldoPorFaturar(Obra obra, Orcamento orcamento) {
        BigDecimal totalOrcamento = totalOrcamentoComIva(orcamento);
        BigDecimal totalJaFaturado = totalFaturado(obra);
        BigDecimal saldo = totalOrcamento.subtract(totalJaFaturado);
        return saldo.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorSubtotalSemIva(Fatura fatura) {
        if (fatura.getValorSubtotalSemIva() != null)
            return CalculosFinanceiros.normalizarDinheiro(fatura.getValorSubtotalSemIva());
        return somarBase(listarResumoIva(fatura));
    }

    public BigDecimal valorIva(Fatura fatura) {
        if (fatura.getValorIva() != null)
            return CalculosFinanceiros.normalizarDinheiro(fatura.getValorIva());
        return somarIva(listarResumoIva(fatura));
    }

    public BigDecimal saldoEmDivida(Fatura fatura) {
        return CalculosFinanceiros.normalizarDinheiro(
                CalculosFinanceiros.normalizarDinheiro(fatura.getValorTotalComIva())
                        .subtract(CalculosFinanceiros.normalizarDinheiro(fatura.getValorPago()))
                        .max(BigDecimal.ZERO));
    }

    public String estadoApresentacao(Fatura fatura) {
        BigDecimal saldo = saldoEmDivida(fatura);
        if (saldo.signum() <= 0) return ESTADO_PAGO;
        LocalDate vencimento = fatura.getDataVencimento();
        if (vencimento != null && vencimento.isBefore(LocalDate.now()))
            return "Vencida";
        if (fatura.getValorPago() != null && fatura.getValorPago().signum() > 0)
            return ESTADO_PARCIAL;
        return fatura.getIdEstadoPagamento() != null
                ? fatura.getIdEstadoPagamento().getNomeEstado()
                : ESTADO_PENDENTE;
    }

    public void eliminar(Integer id) {
        Fatura fatura = buscarPorId(id);
        if (CalculosFinanceiros.normalizarDinheiro(fatura.getValorPago()).compareTo(BigDecimal.ZERO) > 0)
            throw new IllegalStateException("Não é possível eliminar uma fatura com pagamentos registados.");
        faturaResumoIvaRepository.deleteByIdFatura(fatura);
        faturaRepository.deleteById(id);
    }

    private Fatura recalcularPagamentosEEstado(Fatura fatura) {
        BigDecimal totalPago = pagamentoFaturaRepository.findByIdFaturaOrderByDataPagamentoAscIdAsc(fatura).stream()
                .map(PagamentoFatura::getValorPago)
                .map(CalculosFinanceiros::normalizarDinheiro)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        fatura.setValorPago(totalPago);

        if (totalPago.compareTo(CalculosFinanceiros.normalizarDinheiro(fatura.getValorTotalComIva())) >= 0)
            estadopagamentoRepository.findByNomeEstadoIgnoreCase(ESTADO_PAGO).ifPresent(fatura::setIdEstadoPagamento);
        else if (totalPago.signum() > 0)
            estadopagamentoRepository.findByNomeEstadoIgnoreCase(ESTADO_PARCIAL).ifPresent(fatura::setIdEstadoPagamento);
        else
            estadopagamentoRepository.findByNomeEstadoIgnoreCase(ESTADO_PENDENTE).ifPresent(fatura::setIdEstadoPagamento);

        return faturaRepository.save(fatura);
    }

    private void migrarPagamentosAgregadosSeNecessario(Fatura fatura) {
        if (fatura == null) return;
        List<PagamentoFatura> pagamentos = pagamentoFaturaRepository.findByIdFaturaOrderByDataPagamentoAscIdAsc(fatura);
        BigDecimal valorPago = CalculosFinanceiros.normalizarDinheiro(fatura.getValorPago());
        if (!pagamentos.isEmpty() || valorPago.signum() <= 0) return;

        PagamentoFatura sintetico = new PagamentoFatura();
        sintetico.setIdFatura(fatura);
        sintetico.setDataPagamento(fatura.getDataEmissao() != null ? fatura.getDataEmissao() : LocalDate.now());
        sintetico.setValorPago(valorPago);
        sintetico.setMeioPagamento("Registo anterior");
        sintetico.setObservacoes("Pagamento agregado convertido automaticamente em histórico detalhado.");
        pagamentoFaturaRepository.save(sintetico);
    }

    private void guardarResumoIva(Fatura fatura, List<ResumoIva> resumoEmitido) {
        for (ResumoIva resumo : resumoEmitido) {
            FaturaResumoIva entidade = new FaturaResumoIva();
            entidade.setIdFatura(fatura);
            entidade.setTaxaPercentagem(CalculosFinanceiros.normalizarTaxa(resumo.taxaPercentagem()));
            entidade.setValorBase(CalculosFinanceiros.normalizarDinheiro(resumo.baseTributavel()));
            entidade.setValorIva(CalculosFinanceiros.normalizarDinheiro(resumo.valorIva()));
            entidade.setValorTotal(CalculosFinanceiros.normalizarDinheiro(resumo.totalComIva()));
            faturaResumoIvaRepository.save(entidade);
        }
    }

    private void preencherSnapshotCliente(Fatura fatura, Cliente cliente, Obra obra) {
        if (cliente != null) {
            fatura.setClienteNome(cliente.getNome());
            fatura.setClienteNif(cliente.getNif());
            fatura.setClienteMorada(CalculosFinanceiros.formatarMoradaCliente(cliente));
        }
        fatura.setObraMorada(CalculosFinanceiros.formatarMoradaObra(obra));
    }

    private int prazoPagamento(Orcamento orcamento) {
        return orcamento.getPrazoPagamentoDias() != null && orcamento.getPrazoPagamentoDias() > 0
                ? orcamento.getPrazoPagamentoDias()
                : 30;
    }

    private String codigoDocumento(Fatura fatura) {
        int ano = fatura.getDataEmissao() != null ? fatura.getDataEmissao().getYear() : LocalDate.now().getYear();
        return "FT-" + ano + "/" + String.format("%05d", fatura.getId());
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
        return CalculosFinanceiros.totaisLinhas(linhaorcamentoRepository.findByIdOrcamento(orcamento)).totalComIva();
    }

    private List<ResumoIva> saldoIvaPorFaturar(Obra obra, Orcamento orcamento) {
        Map<BigDecimal, BigDecimal[]> saldo = new LinkedHashMap<>();
        for (ResumoIva resumo : CalculosFinanceiros.resumoIva(linhaorcamentoRepository.findByIdOrcamento(orcamento))) {
            saldo.put(resumo.taxaPercentagem(), new BigDecimal[]{
                    resumo.baseTributavel(), resumo.valorIva(), resumo.totalComIva()
            });
        }

        for (Fatura fatura : faturaRepository.findByIdOrcamento(orcamento)) {
            for (ResumoIva resumo : listarResumoIva(fatura)) {
                BigDecimal taxa = CalculosFinanceiros.normalizarTaxa(resumo.taxaPercentagem());
                BigDecimal[] valores = saldo.computeIfAbsent(taxa,
                        ignored -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
                valores[0] = valores[0].subtract(resumo.baseTributavel());
                valores[1] = valores[1].subtract(resumo.valorIva());
                valores[2] = valores[2].subtract(resumo.totalComIva());
            }
        }

        List<ResumoIva> restante = new ArrayList<>();
        for (Map.Entry<BigDecimal, BigDecimal[]> entry : saldo.entrySet()) {
            BigDecimal base = entry.getValue()[0].max(BigDecimal.ZERO);
            BigDecimal iva = entry.getValue()[1].max(BigDecimal.ZERO);
            BigDecimal total = entry.getValue()[2].max(BigDecimal.ZERO);
            if (total.signum() <= 0) continue;
            restante.add(new ResumoIva(
                    entry.getKey(),
                    CalculosFinanceiros.normalizarDinheiro(base),
                    CalculosFinanceiros.normalizarDinheiro(iva),
                    CalculosFinanceiros.normalizarDinheiro(total)));
        }
        return restante;
    }

    private List<ResumoIva> alocarResumoIva(List<ResumoIva> resumoFonte, BigDecimal totalPretendido) {
        BigDecimal totalFonte = resumoFonte.stream()
                .map(ResumoIva::totalComIva)
                .map(CalculosFinanceiros::normalizarDinheiro)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (resumoFonte.isEmpty() || totalFonte.signum() <= 0)
            return List.of();

        BigDecimal restante = CalculosFinanceiros.normalizarDinheiro(totalPretendido);
        List<ResumoIva> resultado = new ArrayList<>();

        for (int i = 0; i < resumoFonte.size(); i++) {
            ResumoIva origem = resumoFonte.get(i);
            BigDecimal totalGrupo = CalculosFinanceiros.normalizarDinheiro(origem.totalComIva());
            BigDecimal alocado;

            if (i == resumoFonte.size() - 1) {
                alocado = restante;
            } else {
                alocado = CalculosFinanceiros.normalizarDinheiro(
                        totalPretendido.multiply(totalGrupo)
                                .divide(totalFonte, 8, RoundingMode.HALF_UP));
                if (alocado.compareTo(totalGrupo) > 0) alocado = totalGrupo;
                if (alocado.compareTo(restante) > 0) alocado = restante;
            }

            BigDecimal fatorIva = BigDecimal.ONE.add(CalculosFinanceiros.percentagem(origem.taxaPercentagem()));
            BigDecimal base = alocado.divide(fatorIva, 2, RoundingMode.HALF_UP);
            BigDecimal iva = CalculosFinanceiros.normalizarDinheiro(alocado.subtract(base));
            resultado.add(new ResumoIva(
                    origem.taxaPercentagem(),
                    CalculosFinanceiros.normalizarDinheiro(base),
                    iva,
                    CalculosFinanceiros.normalizarDinheiro(alocado)));

            restante = CalculosFinanceiros.normalizarDinheiro(restante.subtract(alocado)).max(BigDecimal.ZERO);
        }

        if (restante.signum() > 0 && !resultado.isEmpty()) {
            ResumoIva ultimo = resultado.get(resultado.size() - 1);
            BigDecimal novoTotal = CalculosFinanceiros.normalizarDinheiro(ultimo.totalComIva().add(restante));
            BigDecimal fatorIva = BigDecimal.ONE.add(CalculosFinanceiros.percentagem(ultimo.taxaPercentagem()));
            BigDecimal novaBase = novoTotal.divide(fatorIva, 2, RoundingMode.HALF_UP);
            BigDecimal novoIva = CalculosFinanceiros.normalizarDinheiro(novoTotal.subtract(novaBase));
            resultado.set(resultado.size() - 1, new ResumoIva(ultimo.taxaPercentagem(), novaBase, novoIva, novoTotal));
        }

        return resultado.stream()
                .filter(resumo -> resumo.totalComIva().signum() > 0)
                .toList();
    }

    private BigDecimal somarBase(List<ResumoIva> resumo) {
        return resumo.stream()
                .map(ResumoIva::baseTributavel)
                .map(CalculosFinanceiros::normalizarDinheiro)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal somarIva(List<ResumoIva> resumo) {
        return resumo.stream()
                .map(ResumoIva::valorIva)
                .map(CalculosFinanceiros::normalizarDinheiro)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
