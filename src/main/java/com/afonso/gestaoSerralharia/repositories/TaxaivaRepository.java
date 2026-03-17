package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Taxaiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxaivaRepository extends JpaRepository<Taxaiva, Integer> {
}
