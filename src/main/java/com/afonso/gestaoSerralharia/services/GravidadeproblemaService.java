package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Gravidadeproblema;
import com.afonso.gestaoSerralharia.repositories.GravidadeproblemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GravidadeproblemaService {

    @Autowired
    private GravidadeproblemaRepository gravidadeproblemaRepository;

    public List<Gravidadeproblema> listarTodos() { return gravidadeproblemaRepository.findAll(); }
    public Gravidadeproblema buscarPorId(Integer id) { return gravidadeproblemaRepository.findById(id).orElse(null); }
    public Gravidadeproblema guardar(Gravidadeproblema gp) { return gravidadeproblemaRepository.save(gp); }
    public void eliminar(Integer id) { gravidadeproblemaRepository.deleteById(id); }
}
