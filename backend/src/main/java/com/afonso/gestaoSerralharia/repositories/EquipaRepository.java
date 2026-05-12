package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipaRepository extends JpaRepository<Equipa, Integer> {
    List<Equipa> findByIdObra(Obra obra);
    List<Equipa> findByAtiva(Boolean ativa);
}
