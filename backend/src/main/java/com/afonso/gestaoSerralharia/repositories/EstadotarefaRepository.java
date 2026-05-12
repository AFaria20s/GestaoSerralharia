package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Estadotarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadotarefaRepository extends JpaRepository<Estadotarefa, Integer> {
}
