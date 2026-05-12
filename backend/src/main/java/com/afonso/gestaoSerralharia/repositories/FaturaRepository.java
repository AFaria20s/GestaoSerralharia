package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Integer> {
    List<Fatura> findByIdObra(Obra obra);
    List<Fatura> findByIdObraOrderByNumeroParcelaAsc(Obra obra);
    List<Fatura> findByIdOrcamento(Orcamento orcamento);

    @Query("SELECT f FROM Fatura f WHERE f.idObra = :obra AND f.valorPago < f.valorTotalComIva")
    List<Fatura> findByIdObraAndValorPagoLessThanValorTotalComIva(@Param("obra") Obra obra);
}
