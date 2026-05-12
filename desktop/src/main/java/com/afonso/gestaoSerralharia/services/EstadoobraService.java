package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Estadoobra;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstadoobraService{

    private static final ParameterizedTypeReference<List<Estadoobra>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EstadoobraService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Estadoobra> listarTodos() {
        return api.get("/api/lookups/estados-obra", LIST_TYPE);
    }
    public Estadoobra buscarPorId(Integer id) {
        return api.get("/api/lookups/estados-obra/" + id, Estadoobra.class);
    }
    public Estadoobra guardar(Estadoobra estadoobra) {
        return api.post("/api/lookups/estados-obra", estadoobra, Estadoobra.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/lookups/estados-obra/" + id);
    }
}
