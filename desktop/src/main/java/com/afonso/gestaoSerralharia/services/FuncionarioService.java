package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Funcionario;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FuncionarioService{

    private static final ParameterizedTypeReference<List<Funcionario>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public FuncionarioService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Funcionario> listarTodos() {
        return api.get("/api/funcionarios", LIST_TYPE);
    }
    public Funcionario buscarPorId(Integer id) {
        return api.get("/api/funcionarios/" + id, Funcionario.class);
    }
    public List<Funcionario> buscarPorNome(String nome) {
        return api.get("/api/funcionarios/pesquisa?q=" + api.encode(nome), LIST_TYPE);
    }
    public Funcionario guardar(Funcionario funcionario) {
        if (funcionario.getId() == null) {
            return api.post("/api/funcionarios", funcionario, Funcionario.class);
        }
        return api.put("/api/funcionarios/" + funcionario.getId(), funcionario, Funcionario.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/funcionarios/" + id);
    }
}
