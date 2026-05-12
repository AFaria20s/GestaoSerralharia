package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Visita;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<Visita> listarTodos() {
        return visitaRepository.findAll();
    }

    public Visita buscarPorId(Integer id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visita não encontrada: " + id));
    }

    public List<Visita> buscarPorObra(Obra obra) {
        return visitaRepository.findByIdObra(obra);
    }

    public Visita guardar(Visita visita) {
        if (visita.getIdObra() == null)
            throw new IllegalArgumentException("A visita tem de estar associada a uma obra");
        if (visita.getDataVisita() == null)
            throw new IllegalArgumentException("A data da visita é obrigatória");
        boolean orcamentoAprovado = orcamentoRepository.findFirstByIdObraAndAprovadoTrueOrderByVersaoDesc(visita.getIdObra())
                .map(o -> o.getAprovado()).orElse(false);
        if (orcamentoAprovado)
            throw new IllegalStateException("Não é possível registar visitas numa obra com orçamento já aprovado");
        return visitaRepository.save(visita);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        visitaRepository.deleteById(id);
    }
}
