package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Taxaiva;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaxaivaService{

    private static final ParameterizedTypeReference<List<Taxaiva>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public TaxaivaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Taxaiva> listarTodos() {
        return api.get("/api/lookups/taxas-iva", LIST_TYPE);
    }
    public Taxaiva buscarPorId(Integer id) {
        return api.get("/api/lookups/taxas-iva/" + id, Taxaiva.class);
    }
    public Taxaiva guardar(Taxaiva taxaiva) {
        return api.post("/api/lookups/taxas-iva", taxaiva, Taxaiva.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/lookups/taxas-iva/" + id);
    }
}
