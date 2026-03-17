package com.afonso.gestaoSerralharia.repositories;

import com.afonso.gestaoSerralharia.models.Material;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findByNomeContainingIgnoreCase(String nome);
    List<Material> findByIdFornecedor(Fornecedor fornecedor);
    List<Material> findByStockAtualLessThan(Integer stock);
}
