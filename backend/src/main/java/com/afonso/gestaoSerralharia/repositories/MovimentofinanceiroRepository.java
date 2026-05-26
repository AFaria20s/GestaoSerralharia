package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Movimentofinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentofinanceiroRepository extends JpaRepository<Movimentofinanceiro, Integer> {
    List<Movimentofinanceiro> findByDataMovimentoGreaterThanEqualOrderByDataMovimentoAscIdAsc(LocalDate dataInicio);
    List<Movimentofinanceiro> findAllByOrderByDataMovimentoAscIdAsc();
}
