package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.models.Linhaencomenda;
import com.afonso.gestaoSerralharia.models.Material;
import com.afonso.gestaoSerralharia.repositories.EncomendaRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaencomendaRepository;
import com.afonso.gestaoSerralharia.repositories.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EncomendaService {

    private final EncomendaRepository encomendaRepository;
    private final LinhaencomendaRepository linhaencomendaRepository;
    private final MaterialRepository materialRepository;

    public List<Encomenda> listarTodos() {
        return encomendaRepository.findAll();
    }

    public Encomenda buscarPorId(Integer id) {
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada: " + id));
    }

    public List<Encomenda> buscarPorFornecedor(Fornecedor fornecedor) {
        return encomendaRepository.findByIdFornecedor(fornecedor);
    }

    public List<Encomenda> buscarPorEntregue(Boolean entregue) {
        return encomendaRepository.findByEntregue(entregue);
    }

    public Encomenda guardar(Encomenda encomenda) {
        if (encomenda.getIdFornecedor() == null)
            throw new IllegalArgumentException("A encomenda tem de ter um fornecedor associado");
        return encomendaRepository.save(encomenda);
    }

    public Encomenda marcarComoEntregue(Integer idEncomenda) {
        Encomenda encomenda = buscarPorId(idEncomenda);
        if (encomenda.getEntregue())
            throw new IllegalStateException("Esta encomenda já foi marcada como entregue");
        List<Linhaencomenda> linhas = linhaencomendaRepository.findByIdEncomenda(encomenda);
        if (linhas.isEmpty())
            throw new IllegalStateException("Não é possível entregar uma encomenda sem linhas");
        for (Linhaencomenda linha : linhas) {
            Material material = linha.getIdMaterial();
            material.setStockAtual(material.getStockAtual() + linha.getQuantidade());
            materialRepository.save(material);
        }
        encomenda.setEntregue(true);
        return encomendaRepository.save(encomenda);
    }

    public void eliminar(Integer id) {
        Encomenda encomenda = buscarPorId(id);
        if (encomenda.getEntregue())
            throw new IllegalStateException("Não é possível eliminar uma encomenda já entregue");
        encomendaRepository.deleteById(id);
    }
}
