package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Equipafuncionario;
import com.afonso.gestaoSerralharia.models.EquipafuncionarioId;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipafuncionarioRepository extends JpaRepository<Equipafuncionario, EquipafuncionarioId> {
    List<Equipafuncionario> findByIdEquipa(Equipa equipa);
    List<Equipafuncionario> findByIdFuncionario(Funcionario funcionario);

    @Modifying
    @Query(
            value = "insert into equipafuncionario (id_equipa, id_funcionario) values (:idEquipa, :idFuncionario)",
            nativeQuery = true
    )
    void inserir(@Param("idEquipa") Integer idEquipa, @Param("idFuncionario") Integer idFuncionario);
}
