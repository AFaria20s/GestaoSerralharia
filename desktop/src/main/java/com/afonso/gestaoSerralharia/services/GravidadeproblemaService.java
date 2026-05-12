package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Gravidadeproblema;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GravidadeproblemaService{

    private static final ParameterizedTypeReference<List<Gravidadeproblema>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public GravidadeproblemaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Gravidadeproblema> listarTodos() {
        return api.get("/api/lookups/gravidades-problema", LIST_TYPE);
    }
    public Gravidadeproblema buscarPorId(Integer id) {
        return api.get("/api/lookups/gravidades-problema/" + id, Gravidadeproblema.class);
    }
    public Gravidadeproblema guardar(Gravidadeproblema gravidadeproblema) {
        return api.post("/api/lookups/gravidades-problema", gravidadeproblema, Gravidadeproblema.class);
    }
}
