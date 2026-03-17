package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Estadoobra;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EstadoobraRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.ObraRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final ObraRepository obraRepository;
    private final EstadoobraRepository estadoobraRepository;
    private final LinhaorcamentoRepository linhaorcamentoRepository;

    public List<Orcamento> listarTodos() {
        return orcamentoRepository.findAll();
    }

    public Orcamento buscarPorId(Integer id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado: " + id));
    }

    public Optional<Orcamento> buscarPorObra(Obra obra) {
        return orcamentoRepository.findByIdObra(obra);
    }

    public List<Orcamento> buscarAprovados() {
        return orcamentoRepository.findByAprovado(true);
    }

    public Orcamento guardar(Orcamento orcamento) {
        if (orcamento.getIdObra() == null)
            throw new IllegalArgumentException("O orçamento tem de estar associado a uma obra");
        if (orcamento.getId() == null) {
            boolean jaExiste = orcamentoRepository.existsByIdObra(orcamento.getIdObra());
            if (jaExiste)
                throw new IllegalStateException("Esta obra já tem um orçamento. Edite o orçamento existente");
        }
        return orcamentoRepository.save(orcamento);
    }

    public Orcamento aprovar(Integer idOrcamento) {
        Orcamento orcamento = buscarPorId(idOrcamento);
        if (orcamento.getAprovado())
            throw new IllegalStateException("Este orçamento já foi aprovado");
        if (linhaorcamentoRepository.findByIdOrcamento(orcamento).isEmpty())
            throw new IllegalStateException("Não é possível aprovar um orçamento sem linhas");
        orcamento.setAprovado(true);
        Estadoobra emExecucao = estadoobraRepository.findByNomeEstadoIgnoreCase("Em Execução")
                .orElseThrow(() -> new RuntimeException("Estado 'Em Execução' não encontrado na BD"));
        Obra obra = orcamento.getIdObra();
        obra.setIdEstadoObra(emExecucao);
        obraRepository.save(obra);
        return orcamentoRepository.save(orcamento);
    }

    public void eliminar(Integer id) {
        Orcamento orcamento = buscarPorId(id);
        if (orcamento.getAprovado())
            throw new IllegalStateException("Não é possível eliminar um orçamento aprovado");
        orcamentoRepository.deleteById(id);
    }
}
