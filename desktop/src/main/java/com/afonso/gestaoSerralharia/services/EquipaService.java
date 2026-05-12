package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Obra;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipaService{

    private static final ParameterizedTypeReference<List<Equipa>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EquipaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Equipa> listarTodos() {
        return api.get("/api/equipas", LIST_TYPE);
    }
    public Equipa buscarPorId(Integer id) {
        return api.get("/api/equipas/" + id, Equipa.class);
    }
    public List<Equipa> buscarPorObra(Obra obra) {
        return api.get("/api/equipas/obra/" + obra.getId(), LIST_TYPE);
    }
    public List<Equipa> buscarAtivas() {
        return api.get("/api/equipas/ativas", LIST_TYPE);
    }
    public Equipa guardar(Equipa equipa) {
        if (equipa.getId() == null) {
            return api.post("/api/equipas", equipa, Equipa.class);
        }
        return api.put("/api/equipas/" + equipa.getId(), equipa, Equipa.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/equipas/" + id);
    }
}
