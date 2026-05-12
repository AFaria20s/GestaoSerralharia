package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Codpostal;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodpostalService{

    private static final ParameterizedTypeReference<List<Codpostal>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public CodpostalService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Codpostal> listarTodos() {
        return api.get("/api/lookups/codpostais", LIST_TYPE);
    }
    public Codpostal buscarPorId(Integer id) {
        return api.get("/api/lookups/codpostais/" + id, Codpostal.class);
    }
    public List<Codpostal> buscarPorCodpostal(String codpostal) {
        return api.get("/api/lookups/codpostais/pesquisa?q=" + api.encode(codpostal), LIST_TYPE);
    }
    public Codpostal guardar(Codpostal codpostal) {
        return api.post("/api/lookups/codpostais", codpostal, Codpostal.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/lookups/codpostais/" + id);
    }
    public Codpostal encontrarOuCriar(String texto) {
        return api.post("/api/lookups/codpostais/encontrar-ou-criar", texto, Codpostal.class);
    }
}
