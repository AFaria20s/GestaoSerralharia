package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Visita;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VisitaService{

    private static final ParameterizedTypeReference<List<Visita>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public VisitaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Visita> listarTodos() {
        return api.get("/api/visitas", LIST_TYPE);
    }
    public Visita buscarPorId(Integer id) {
        return api.get("/api/visitas/" + id, Visita.class);
    }
    public List<Visita> buscarPorObra(Obra obra) {
        return api.get("/api/visitas/obra/" + obra.getId(), LIST_TYPE);
    }
    public Visita guardar(Visita visita) {
        if (visita.getId() == null) {
            return api.post("/api/visitas", visita, Visita.class);
        }
        return api.put("/api/visitas/" + visita.getId(), visita, Visita.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/visitas/" + id);
    }
}
