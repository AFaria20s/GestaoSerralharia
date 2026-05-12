package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Tipolinhaorcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipolinhaorcamentoRepository extends JpaRepository<Tipolinhaorcamento, Integer> {
}
