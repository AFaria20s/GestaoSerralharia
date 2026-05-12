package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FornecedorService{

    private static final ParameterizedTypeReference<List<Fornecedor>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public FornecedorService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Fornecedor> listarTodos() {
        return api.get("/api/fornecedores", LIST_TYPE);
    }
    public Fornecedor buscarPorId(Integer id) {
        return api.get("/api/fornecedores/" + id, Fornecedor.class);
    }
    public List<Fornecedor> buscarPorNome(String nome) {
        return api.get("/api/fornecedores/pesquisa?q=" + api.encode(nome), LIST_TYPE);
    }
    public Fornecedor guardar(Fornecedor fornecedor) {
        if (fornecedor.getId() == null) {
            return api.post("/api/fornecedores", fornecedor, Fornecedor.class);
        }
        return api.put("/api/fornecedores/" + fornecedor.getId(), fornecedor, Fornecedor.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/fornecedores/" + id);
    }
}
