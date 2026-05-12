package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.FaturaResumoIva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaturaResumoIvaRepository extends JpaRepository<FaturaResumoIva, Integer> {
    List<FaturaResumoIva> findByIdFaturaOrderByTaxaPercentagemAsc(Fatura fatura);
    void deleteByIdFatura(Fatura fatura);
}
