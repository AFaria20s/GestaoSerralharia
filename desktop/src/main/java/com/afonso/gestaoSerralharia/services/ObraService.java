package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.models.Obra;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObraService{

    private static final ParameterizedTypeReference<List<Obra>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public ObraService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Obra> listarTodos() {
        return api.get("/api/obras", LIST_TYPE);
    }
    public Obra buscarPorId(Integer id) {
        return api.get("/api/obras/" + id, Obra.class);
    }
    public List<Obra> buscarPorCliente(Cliente cliente) {
        return api.get("/api/obras/cliente/" + cliente.getId(), LIST_TYPE);
    }
    public Obra guardar(Obra obra) {
        if (obra.getId() == null) {
            return api.post("/api/obras", obra, Obra.class);
        }
        return api.put("/api/obras/" + obra.getId(), obra, Obra.class);
    }
    public Obra finalizar(Integer idObra) {
        return api.post("/api/obras/" + idObra + "/finalizar", null, Obra.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/obras/" + id);
    }
}
