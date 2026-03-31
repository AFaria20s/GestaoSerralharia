package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EquipaRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipaService {

    private final EquipaRepository equipaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<Equipa> listarTodos() {
        return equipaRepository.findAll();
    }

    public Equipa buscarPorId(Integer id) {
        return equipaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipa não encontrada: " + id));
    }

    public List<Equipa> buscarPorObra(Obra obra) {
        return equipaRepository.findByIdObra(obra);
    }

    public List<Equipa> buscarAtivas() {
        return equipaRepository.findByAtiva(true);
    }

    public Equipa guardar(Equipa equipa) {
        if (equipa.getNomeEquipa() == null || equipa.getNomeEquipa().isBlank())
            throw new IllegalArgumentException("O nome da equipa é obrigatório");
        if (equipa.getIdObra() == null)
            throw new IllegalArgumentException("A equipa tem de estar associada a uma obra");
        return equipaRepository.save(equipa);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        equipaRepository.deleteById(id);
    }
}
