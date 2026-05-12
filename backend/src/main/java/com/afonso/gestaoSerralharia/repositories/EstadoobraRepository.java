package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Estadoobra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoobraRepository extends JpaRepository<Estadoobra, Integer> {
    Optional<Estadoobra> findByNomeEstadoIgnoreCase(String nomeEstado);
}
