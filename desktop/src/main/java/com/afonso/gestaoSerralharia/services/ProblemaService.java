package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Problema;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemaService{

    private static final ParameterizedTypeReference<List<Problema>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public ProblemaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Problema> listarTodos() {
        return api.get("/api/problemas", LIST_TYPE);
    }
    public Problema buscarPorId(Integer id) {
        return api.get("/api/problemas/" + id, Problema.class);
    }
    public List<Problema> buscarPorObra(Obra obra) {
        return api.get("/api/problemas/obra/" + obra.getId(), LIST_TYPE);
    }
    public Problema guardar(Problema problema) {
        if (problema.getId() == null) {
            return api.post("/api/problemas", problema, Problema.class);
        }
        return api.put("/api/problemas/" + problema.getId(), problema, Problema.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/problemas/" + id);
    }
}
