package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Cargo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CargoService{

    private static final ParameterizedTypeReference<List<Cargo>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public CargoService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Cargo> listarTodos() {
        return api.get("/api/cargos", LIST_TYPE);
    }
    public Cargo buscarPorId(Integer id) {
        return api.get("/api/cargos/" + id, Cargo.class);
    }
    public List<Cargo> buscarPorNome(String nome) {
        return api.get("/api/cargos/pesquisa?q=" + api.encode(nome), LIST_TYPE);
    }
    public Cargo guardar(Cargo cargo) {
        if (cargo.getId() == null) {
            return api.post("/api/cargos", cargo, Cargo.class);
        }
        return api.put("/api/cargos/" + cargo.getId(), cargo, Cargo.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/cargos/" + id);
    }
}
