package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Codpostal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodpostalRepository extends JpaRepository<Codpostal, Integer> {
    List<Codpostal> findByCodpostalContainingIgnoreCase(String codpostal);
}
