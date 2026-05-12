package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncomendaRepository extends JpaRepository<Encomenda, Integer> {
    List<Encomenda> findByIdFornecedor(Fornecedor fornecedor);
    List<Encomenda> findByEntregue(Boolean entregue);
}
