package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinhaorcamentoService {

    private final LinhaorcamentoRepository linhaorcamentoRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<Linhaorcamento> listarTodos() {
        return linhaorcamentoRepository.findAll();
    }

    public Linhaorcamento buscarPorId(Integer id) {
        return linhaorcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Linha de orçamento não encontrada: " + id));
    }

    public List<Linhaorcamento> buscarPorOrcamento(Orcamento orcamento) {
        return linhaorcamentoRepository.findByIdOrcamento(orcamento);
    }

    public Linhaorcamento guardar(Linhaorcamento linha) {
        if (linha.getIdOrcamento() == null)
            throw new IllegalArgumentException("A linha tem de estar associada a um orçamento");
        Orcamento orcamento = orcamentoRepository.findById(linha.getIdOrcamento().getId())
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
        if (orcamento.getAprovado())
            throw new IllegalStateException("Não é possível alterar linhas de um orçamento já aprovado");
        if (linha.getPrecoUnit() == null || linha.getPrecoUnit().signum() <= 0)
            throw new IllegalArgumentException("O preço unitário tem de ser maior que zero");
        if (linha.getQuantidade() == null || linha.getQuantidade().signum() <= 0)
            throw new IllegalArgumentException("A quantidade tem de ser maior que zero");
        if (linha.getIvaPercentagemAplicada() == null)
            throw new IllegalArgumentException("A percentagem de IVA é obrigatória");
        return linhaorcamentoRepository.save(linha);
    }

    public void eliminar(Integer id) {
        Linhaorcamento linha = buscarPorId(id);
        if (linha.getIdOrcamento().getAprovado())
            throw new IllegalStateException("Não é possível eliminar linhas de um orçamento aprovado");
        linhaorcamentoRepository.deleteById(id);
    }
}
