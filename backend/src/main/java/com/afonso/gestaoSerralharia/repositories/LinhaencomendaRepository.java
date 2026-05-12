package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Linhaencomenda;
import com.afonso.gestaoSerralharia.models.Encomenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinhaencomendaRepository extends JpaRepository<Linhaencomenda, Integer> {
    List<Linhaencomenda> findByIdEncomenda(Encomenda encomenda);
}
