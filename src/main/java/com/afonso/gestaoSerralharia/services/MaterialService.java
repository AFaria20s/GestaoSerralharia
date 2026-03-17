package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.models.Material;
import com.afonso.gestaoSerralharia.repositories.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    public Material buscarPorId(Integer id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado: " + id));
    }

    public List<Material> buscarPorNome(String nome) {
        return materialRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Material> buscarPorFornecedor(Fornecedor fornecedor) {
        return materialRepository.findByIdFornecedor(fornecedor);
    }

    public List<Material> buscarStockBaixo(Integer limite) {
        return materialRepository.findByStockAtualLessThan(limite);
    }

    public Material guardar(Material material) {
        if (material.getNome() == null || material.getNome().isBlank())
            throw new IllegalArgumentException("Nome do material é obrigatório");
        if (material.getStockAtual() != null && material.getStockAtual() < 0)
            throw new IllegalArgumentException("O stock não pode ser negativo");
        return materialRepository.save(material);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        materialRepository.deleteById(id);
    }
}
