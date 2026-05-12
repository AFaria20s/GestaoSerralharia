package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {
    List<Fornecedor> findByNomeContainingIgnoreCase(String nome);
    Fornecedor findByNif(String nif);
    Fornecedor findByEmail(String email);
}
