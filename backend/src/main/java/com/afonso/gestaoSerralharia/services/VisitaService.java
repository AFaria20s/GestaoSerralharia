package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Visita;
import com.afonso.gestaoSerralharia.repositories.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitaService {

    private final VisitaRepository visitaRepository;

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
        Obra obra = visita.getIdObra();
        String estadoObra = obra.getIdEstadoObra() != null ? obra.getIdEstadoObra().getNomeEstado() : null;

        // Não faz sentido bloquear o dono de criar uma visita a uma obra concluida
        /*
        if (estadoObra != null && (estadoObra.equalsIgnoreCase("Concluída") || estadoObra.equalsIgnoreCase("Concluida"))) {
            throw new IllegalStateException("Não é possível registar visitas numa obra concluída");
        }
        */
        return visitaRepository.save(visita);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        visitaRepository.deleteById(id);
    }
}
