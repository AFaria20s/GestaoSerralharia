package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.models.Estadoobra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Integer> {
    List<Obra> findByIdCliente(Cliente cliente);
    List<Obra> findByIdEstadoObra(Estadoobra estadoObra);
}
