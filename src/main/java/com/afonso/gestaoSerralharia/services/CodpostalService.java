package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Codpostal;
import com.afonso.gestaoSerralharia.repositories.CodpostalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodpostalService {

    @Autowired
    private CodpostalRepository codpostalRepository;

    public List<Codpostal> listarTodos() { return codpostalRepository.findAll(); }

    public Codpostal buscarPorId(Integer id) { return codpostalRepository.findById(id).orElse(null); }

    public List<Codpostal> buscarPorCodpostal(String codpostal) {
        return codpostalRepository.findByCodpostalContainingIgnoreCase(codpostal);
    }

    public Codpostal guardar(Codpostal codpostal) { return codpostalRepository.save(codpostal); }

    public void eliminar(Integer id) { codpostalRepository.deleteById(id); }

    public Codpostal encontrarOuCriar(String texto) {
        String limpo = texto == null ? "" : texto.trim();
        if (limpo.isBlank())
            throw new IllegalArgumentException("O código postal não pode estar vazio.");

        return codpostalRepository.findByCodpostalContainingIgnoreCase(limpo)
                .stream()
                .filter(cp -> cp.getCodpostal().equalsIgnoreCase(limpo))
                .findFirst()
                .orElseGet(() -> {
                    Codpostal novo = new Codpostal();
                    novo.setCodpostal(limpo);
                    return codpostalRepository.save(novo);
                });
    }
}