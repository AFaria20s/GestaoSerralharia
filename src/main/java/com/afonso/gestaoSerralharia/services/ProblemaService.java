package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Problema;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.ProblemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemaService {

    private final ProblemaRepository problemaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<Problema> listarTodos() {
        return problemaRepository.findAll();
    }

    public Problema buscarPorId(Integer id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problema não encontrado: " + id));
    }

    public List<Problema> buscarPorObra(Obra obra) {
        return problemaRepository.findByIdObra(obra);
    }

    public List<Problema> buscarPorTarefa(Tarefa tarefa) {
        return problemaRepository.findByIdTarefa(tarefa);
    }

    public Problema guardar(Problema problema) {
        if (problema.getIdObra() == null)
            throw new IllegalArgumentException("O problema tem de estar associado a uma obra");
        if (problema.getDescricao() == null || problema.getDescricao().isBlank())
            throw new IllegalArgumentException("A descrição do problema é obrigatória");
        boolean orcamentoAprovado = orcamentoRepository.findFirstByIdObraAndAprovadoTrueOrderByVersaoDesc(problema.getIdObra())
                .map(o -> o.getAprovado()).orElse(false);
        if (!orcamentoAprovado)
            throw new IllegalStateException("Só é possível reportar problemas em obras em execução");
        if (problema.getDataReporte() == null)
            problema.setDataReporte(Instant.now());
        return problemaRepository.save(problema);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        problemaRepository.deleteById(id);
    }
}
