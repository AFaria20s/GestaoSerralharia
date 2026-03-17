package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<Tarefa> listarTodos() {
        return tarefaRepository.findAll();
    }

    public Tarefa buscarPorId(Integer id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + id));
    }

    public List<Tarefa> buscarPorObra(Obra obra) {
        return tarefaRepository.findByIdObra(obra);
    }

    public List<Tarefa> buscarPorFuncionario(Funcionario funcionario) {
        return tarefaRepository.findByIdFuncionario(funcionario);
    }

    public List<Tarefa> buscarAtrasadas() {
        return tarefaRepository.findByDataLimiteBefore(LocalDate.now());
    }

    public Tarefa guardar(Tarefa tarefa) {
        if (tarefa.getIdObra() == null)
            throw new IllegalArgumentException("A tarefa tem de estar associada a uma obra");
        if (tarefa.getIdFuncionario() == null)
            throw new IllegalArgumentException("A tarefa tem de ter um funcionário atribuído");
        if (tarefa.getDataLimite() == null)
            throw new IllegalArgumentException("A data limite é obrigatória");
        Orcamento orcamento = orcamentoRepository.findByIdObra(tarefa.getIdObra()).orElse(null);
        if (orcamento == null || !orcamento.getAprovado())
            throw new IllegalStateException("Só é possível criar tarefas em obras com orçamento aprovado");
        return tarefaRepository.save(tarefa);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        tarefaRepository.deleteById(id);
    }
}
