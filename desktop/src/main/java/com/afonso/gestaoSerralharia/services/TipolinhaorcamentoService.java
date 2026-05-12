package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Tipolinhaorcamento;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TipolinhaorcamentoService{

    private static final ParameterizedTypeReference<List<Tipolinhaorcamento>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public TipolinhaorcamentoService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Tipolinhaorcamento> listarTodos() {
        return api.get("/api/lookups/tipos-linha-orcamento", LIST_TYPE);
    }
    public Tipolinhaorcamento buscarPorId(Integer id) {
        return api.get("/api/lookups/tipos-linha-orcamento/" + id, Tipolinhaorcamento.class);
    }
    public Tipolinhaorcamento guardar(Tipolinhaorcamento tipo) {
        return api.post("/api/lookups/tipos-linha-orcamento", tipo, Tipolinhaorcamento.class);
    }
}
