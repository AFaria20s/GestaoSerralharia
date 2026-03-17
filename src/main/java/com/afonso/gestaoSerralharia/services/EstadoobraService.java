package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Estadoobra;
import com.afonso.gestaoSerralharia.repositories.EstadoobraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoobraService {

    @Autowired
    private EstadoobraRepository estadoobraRepository;

    public List<Estadoobra> listarTodos() { return estadoobraRepository.findAll(); }
    public Estadoobra buscarPorId(Integer id) { return estadoobraRepository.findById(id).orElse(null); }
    public Estadoobra guardar(Estadoobra estadoobra) { return estadoobraRepository.save(estadoobra); }
    public void eliminar(Integer id) { estadoobraRepository.deleteById(id); }
}
