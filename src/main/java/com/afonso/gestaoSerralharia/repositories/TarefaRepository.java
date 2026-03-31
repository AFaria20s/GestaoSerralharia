package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {
    List<Tarefa> findByIdObra(Obra obra);
    List<Tarefa> findByIdFuncionario(Funcionario funcionario);
    List<Tarefa> findByIdEquipa(Equipa equipa);
    List<Tarefa> findByDataLimiteBefore(LocalDate data);
}
