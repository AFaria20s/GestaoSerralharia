package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    List<Funcionario> findByNomeContainingIgnoreCase(String nome);
    Funcionario findByEmail(String email);
    List<Funcionario> findByIdCargo(Cargo cargo);
}
