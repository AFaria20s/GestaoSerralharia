package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Tarefa;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TarefaService{

    private static final ParameterizedTypeReference<List<Tarefa>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public TarefaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Tarefa> listarTodos() {
        return api.get("/api/tarefas", LIST_TYPE);
    }
    public Tarefa buscarPorId(Integer id) {
        return api.get("/api/tarefas/" + id, Tarefa.class);
    }
    public List<Tarefa> buscarPorObra(Obra obra) {
        return api.get("/api/tarefas/obra/" + obra.getId(), LIST_TYPE);
    }
    public List<Tarefa> buscarPorFuncionario(Funcionario funcionario) {
        return api.get("/api/tarefas/funcionario/" + funcionario.getId(), LIST_TYPE);
    }
    public List<Tarefa> buscarPorEquipa(Equipa equipa) {
        return api.get("/api/tarefas/equipa/" + equipa.getId(), LIST_TYPE);
    }
    public List<Tarefa> buscarAtrasadas() {
        return api.get("/api/tarefas/atrasadas", LIST_TYPE);
    }
    public Tarefa guardar(Tarefa tarefa) {
        if (tarefa.getId() == null) {
            return api.post("/api/tarefas", tarefa, Tarefa.class);
        }
        return api.put("/api/tarefas/" + tarefa.getId(), tarefa, Tarefa.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/tarefas/" + id);
    }
}
