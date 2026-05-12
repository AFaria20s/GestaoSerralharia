package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Estadotarefa;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstadotarefaService{

    private static final ParameterizedTypeReference<List<Estadotarefa>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EstadotarefaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Estadotarefa> listarTodos() {
        return api.get("/api/lookups/estados-tarefa", LIST_TYPE);
    }
    public Estadotarefa buscarPorId(Integer id) {
        return api.get("/api/lookups/estados-tarefa/" + id, Estadotarefa.class);
    }
    public Estadotarefa guardar(Estadotarefa estadotarefa) {
        return api.post("/api/lookups/estados-tarefa", estadotarefa, Estadotarefa.class);
    }
}
