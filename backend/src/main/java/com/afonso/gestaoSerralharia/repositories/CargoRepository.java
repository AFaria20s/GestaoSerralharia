package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {
    List<Cargo> findByNomeContainingIgnoreCase(String nome);
}
