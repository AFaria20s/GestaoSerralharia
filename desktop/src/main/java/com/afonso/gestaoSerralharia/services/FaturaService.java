package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.util.TotaisFinanceiros;
import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.models.PagamentoFatura;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FaturaService{

    private static final ParameterizedTypeReference<List<Fatura>> FATURA_LIST_TYPE = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<ResumoIva>> RESUMO_IVA_LIST_TYPE = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<PagamentoFatura>> PAGAMENTO_LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;
    private final OrcamentoService orcamentoService;

    public FaturaService(DesktopApiSupport api, OrcamentoService orcamentoService) {
        this.api = api;
        this.orcamentoService = orcamentoService;
    }
    public List<Fatura> listarTodos() {
        return api.get("/api/faturas", FATURA_LIST_TYPE);
    }
    public Fatura buscarPorId(Integer id) {
        return api.get("/api/faturas/" + id, Fatura.class);
    }
    public List<Fatura> buscarPorObra(Obra obra) {
        return api.get("/api/faturas/obra/" + obra.getId(), FATURA_LIST_TYPE);
    }
    public List<PagamentoFatura> listarPagamentos(Fatura fatura) {
        return api.get("/api/faturas/" + fatura.getId() + "/pagamentos", PAGAMENTO_LIST_TYPE);
    }
    public List<ResumoIva> listarResumoIva(Fatura fatura) {
        return api.get("/api/faturas/" + fatura.getId() + "/resumo-iva", RESUMO_IVA_LIST_TYPE);
    }
    public Fatura emitir(Obra obra) {
        return api.post("/api/faturas/obra/" + obra.getId() + "/emitir", null, Fatura.class);
    }
    public Fatura emitir(Obra obra, BigDecimal valor, String descricao) {
        return api.post("/api/faturas/obra/" + obra.getId() + "/emitir-parcial",
                Map.of("valor", valor, "descricao", descricao), Fatura.class);
    }
    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago) {
        return registarPagamento(idFatura, valorPago, LocalDate.now(), "Transferência Bancária", null, null);
    }
    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago, LocalDate dataPagamento,
                                    String meioPagamento, String referencia, String observacoes) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("valor", valorPago);
        payload.put("data", dataPagamento);
        payload.put("meio", meioPagamento);
        if (referencia != null) payload.put("referencia", referencia);
        if (observacoes != null) payload.put("observacoes", observacoes);
        return api.post("/api/faturas/" + idFatura + "/pagamento", payload, Fatura.class);
    }
    public BigDecimal totalFaturado(Obra obra) {
        return api.get("/api/faturas/obra/" + obra.getId() + "/total-faturado", BigDecimal.class);
    }
    public BigDecimal saldoPorFaturar(Obra obra, Orcamento orcamento) {
        TotaisFinanceiros totais = orcamentoService.calcularTotais(orcamento);
        return totais.totalComIva().subtract(totalFaturado(obra)).max(BigDecimal.ZERO);
    }
    public BigDecimal valorSubtotalSemIva(Fatura fatura) {
        if (fatura.getValorSubtotalSemIva() != null) {
            return fatura.getValorSubtotalSemIva();
        }
        return listarResumoIva(fatura).stream()
                .map(ResumoIva::baseTributavel)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal valorIva(Fatura fatura) {
        if (fatura.getValorIva() != null) {
            return fatura.getValorIva();
        }
        return listarResumoIva(fatura).stream()
                .map(ResumoIva::valorIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal saldoEmDivida(Fatura fatura) {
        return api.get("/api/faturas/" + fatura.getId() + "/saldo-divida", BigDecimal.class);
    }
    public String estadoApresentacao(Fatura fatura) {
        return api.get("/api/faturas/" + fatura.getId() + "/estado", String.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/faturas/" + id);
    }
}
