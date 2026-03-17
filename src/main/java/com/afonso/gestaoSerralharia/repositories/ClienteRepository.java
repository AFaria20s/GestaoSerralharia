package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
    Cliente findByNif(String nif);
}
