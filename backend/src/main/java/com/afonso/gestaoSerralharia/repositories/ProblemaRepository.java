package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Problema;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemaRepository extends JpaRepository<Problema, Integer> {
    List<Problema> findByIdObra(Obra obra);
    List<Problema> findByIdTarefa(Tarefa tarefa);
}
