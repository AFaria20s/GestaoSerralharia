package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinhaorcamentoRepository extends JpaRepository<Linhaorcamento, Integer> {
    List<Linhaorcamento> findByIdOrcamento(Orcamento orcamento);
}
