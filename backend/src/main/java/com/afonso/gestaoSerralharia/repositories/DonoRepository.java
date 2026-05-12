package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Dono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonoRepository extends JpaRepository<Dono, Integer> {
    Dono findByEmail(String email);
}
