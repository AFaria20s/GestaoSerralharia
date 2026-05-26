package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.repositories.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final EquipafuncionarioService equipafuncionarioService;

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

    public List<Tarefa> buscarPorEquipa(Equipa equipa) {
        return tarefaRepository.findByIdEquipa(equipa);
    }

    public List<Tarefa> buscarAtrasadas() {
        return tarefaRepository.findByDataLimiteBefore(LocalDate.now());
    }

    public Tarefa guardar(Tarefa tarefa) {
        if (tarefa.getIdObra() == null)
            throw new IllegalArgumentException("A tarefa tem de estar associada a uma obra");
        if (tarefa.getIdFuncionario() == null)
            throw new IllegalArgumentException("A tarefa tem de ter um funcionário atribuído");
        if (tarefa.getIdEquipa() == null)
            throw new IllegalArgumentException("A tarefa tem de estar associada a uma equipa");
        if (tarefa.getDataLimite() == null)
            throw new IllegalArgumentException("A data limite é obrigatória");
        if (tarefa.getIdEquipa().getIdObra() == null || tarefa.getIdObra().getId() == null
                || !tarefa.getIdObra().getId().equals(tarefa.getIdEquipa().getIdObra().getId()))
            throw new IllegalStateException("A equipa escolhida não pertence à obra da tarefa");
        if (!equipafuncionarioService.pertenceAEquipa(tarefa.getIdEquipa(), tarefa.getIdFuncionario()))
            throw new IllegalStateException("O funcionário atribuído não pertence à equipa selecionada");
        return tarefaRepository.save(tarefa);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        tarefaRepository.deleteById(id);
    }
}
