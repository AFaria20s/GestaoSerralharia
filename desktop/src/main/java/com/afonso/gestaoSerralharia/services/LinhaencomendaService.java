package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.models.Linhaencomenda;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinhaencomendaService{

    private static final ParameterizedTypeReference<List<Linhaencomenda>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public LinhaencomendaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Linhaencomenda> buscarPorEncomenda(Encomenda encomenda) {
        return api.get("/api/encomendas/" + encomenda.getId() + "/linhas", LIST_TYPE);
    }
    public Linhaencomenda guardar(Linhaencomenda linha) {
        return api.post("/api/encomendas/" + linha.getIdEncomenda().getId() + "/linhas", linha, Linhaencomenda.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/encomendas/linhas/" + id);
    }
}
