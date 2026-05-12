package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Orcamento;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinhaorcamentoService{

    private static final ParameterizedTypeReference<List<Linhaorcamento>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public LinhaorcamentoService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Linhaorcamento> listarTodos() {
        return api.get("/api/linhas-orcamento", LIST_TYPE);
    }
    public Linhaorcamento buscarPorId(Integer id) {
        return api.get("/api/linhas-orcamento/" + id, Linhaorcamento.class);
    }
    public List<Linhaorcamento> buscarPorOrcamento(Orcamento orcamento) {
        return api.get("/api/linhas-orcamento/orcamento/" + orcamento.getId(), LIST_TYPE);
    }
    public Linhaorcamento guardar(Linhaorcamento linha) {
        if (linha.getId() == null) {
            return api.post("/api/linhas-orcamento", linha, Linhaorcamento.class);
        }
        return api.put("/api/linhas-orcamento/" + linha.getId(), linha, Linhaorcamento.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/linhas-orcamento/" + id);
    }
}
