package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Visita;
import com.afonso.gestaoSerralharia.models.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Integer> {
    List<Visita> findByIdObra(Obra obra);
}
