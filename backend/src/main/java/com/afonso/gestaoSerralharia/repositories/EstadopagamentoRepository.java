package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Estadopagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadopagamentoRepository extends JpaRepository<Estadopagamento, Integer> {
    Optional<Estadopagamento> findByNomeEstadoIgnoreCase(String nomeEstado);
}
