package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.ApiClientException;
import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.util.TotaisFinanceiros;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class OrcamentoService{

    private static final ParameterizedTypeReference<List<Orcamento>> ORCAMENTO_LIST_TYPE = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<ResumoIva>> RESUMO_IVA_LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public OrcamentoService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Orcamento> listarTodos() {
        return api.get("/api/orcamentos", ORCAMENTO_LIST_TYPE);
    }

    public Orcamento buscarPorId(Integer id) {
        return api.get("/api/orcamentos/" + id, Orcamento.class);
    }
    public Optional<Orcamento> buscarPorObra(Obra obra) {
        try {
            return Optional.ofNullable(api.get("/api/orcamentos/obra/" + obra.getId() + "/ativo", Orcamento.class));
        } catch (ApiClientException ex) {
            return Optional.empty();
        }
    }
    public Optional<Orcamento> buscarAprovadoPorObra(Obra obra) {
        try {
            return Optional.ofNullable(api.get("/api/orcamentos/obra/" + obra.getId() + "/aprovado", Orcamento.class));
        } catch (ApiClientException ex) {
            return Optional.empty();
        }
    }
    public List<Orcamento> buscarAprovados() {
        return api.get("/api/orcamentos/aprovados", ORCAMENTO_LIST_TYPE);
    }
    public Orcamento guardar(Orcamento orcamento) {
        if (orcamento.getId() == null) {
            return api.post("/api/orcamentos", orcamento, Orcamento.class);
        }
        return api.put("/api/orcamentos/" + orcamento.getId(), orcamento, Orcamento.class);
    }
    public Orcamento aprovar(Integer idOrcamento) {
        return api.post("/api/orcamentos/" + idOrcamento + "/aprovar", null, Orcamento.class);
    }
    public Orcamento criarRevisao(Integer idOrcamentoBase) {
        return api.post("/api/orcamentos/" + idOrcamentoBase + "/revisao", null, Orcamento.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/orcamentos/" + id);
    }
    public TotaisFinanceiros calcularTotais(Orcamento orcamento) {
        return api.get("/api/orcamentos/" + orcamento.getId() + "/totais", TotaisFinanceiros.class);
    }
    public List<ResumoIva> calcularResumoIva(Orcamento orcamento) {
        return api.get("/api/orcamentos/" + orcamento.getId() + "/resumo-iva", RESUMO_IVA_LIST_TYPE);
    }
    public LocalDate calcularDataValidade(Orcamento orcamento) {
        return api.get("/api/orcamentos/" + orcamento.getId() + "/data-validade", LocalDate.class);
    }
}
