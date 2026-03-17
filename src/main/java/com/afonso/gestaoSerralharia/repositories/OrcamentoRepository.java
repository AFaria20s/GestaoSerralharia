package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {
    Optional<Orcamento> findByIdObra(Obra obra);
    List<Orcamento> findByAprovado(Boolean aprovado);
    boolean existsByIdObra(Obra obra);
}
