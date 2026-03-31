package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.models.Material;
import com.afonso.gestaoSerralharia.repositories.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        if (material.getStockReservado() != null && material.getStockReservado().signum() < 0)
            throw new IllegalArgumentException("O stock reservado não pode ser negativo");
        return materialRepository.save(material);
    }

    public BigDecimal stockReservado(Material material) {
        if (material == null || material.getStockReservado() == null) return BigDecimal.ZERO;
        return material.getStockReservado();
    }

    public BigDecimal stockDisponivel(Material material) {
        if (material == null) return BigDecimal.ZERO;
        BigDecimal atual = BigDecimal.valueOf(material.getStockAtual() != null ? material.getStockAtual() : 0);
        return atual.subtract(stockReservado(material));
    }

    public void reservar(Material material, BigDecimal quantidade) {
        if (material == null || quantidade == null || quantidade.signum() <= 0) return;
        BigDecimal disponivel = stockDisponivel(material);
        if (disponivel.compareTo(quantidade) < 0)
            throw new IllegalStateException("Stock insuficiente para reservar o material '" + material.getNome() + "'");
        material.setStockReservado(stockReservado(material).add(quantidade));
        materialRepository.save(material);
    }

    public void libertarReserva(Material material, BigDecimal quantidade) {
        if (material == null || quantidade == null || quantidade.signum() <= 0) return;
        BigDecimal novoReservado = stockReservado(material).subtract(quantidade);
        if (novoReservado.signum() < 0) novoReservado = BigDecimal.ZERO;
        material.setStockReservado(novoReservado);
        materialRepository.save(material);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        materialRepository.deleteById(id);
    }
}
