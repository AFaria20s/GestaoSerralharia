package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Cliente;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClienteService{

    private static final ParameterizedTypeReference<List<Cliente>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public ClienteService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Cliente> listarTodos() {
        return api.get("/api/clientes", LIST_TYPE);
    }
    public Cliente buscarPorId(Integer id) {
        return api.get("/api/clientes/" + id, Cliente.class);
    }
    public Cliente buscarPorNif(String nif) {
        return api.get("/api/clientes/nif/" + nif, Cliente.class);
    }
    public List<Cliente> buscarPorNome(String nome) {
        return api.get("/api/clientes/pesquisa?q=" + api.encode(nome), LIST_TYPE);
    }
    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null) {
            return api.post("/api/clientes", cliente, Cliente.class);
        }
        return api.put("/api/clientes/" + cliente.getId(), cliente, Cliente.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/clientes/" + id);
    }
}
