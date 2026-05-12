package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.PagamentoFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoFaturaRepository extends JpaRepository<PagamentoFatura, Integer> {
    List<PagamentoFatura> findByIdFaturaOrderByDataPagamentoAscIdAsc(Fatura fatura);
}
