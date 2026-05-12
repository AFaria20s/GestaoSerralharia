package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Estadotarefa;
import com.afonso.gestaoSerralharia.repositories.EstadotarefaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadotarefaService {

    @Autowired
    private EstadotarefaRepository estadotarefaRepository;

    public List<Estadotarefa> listarTodos() { return estadotarefaRepository.findAll(); }
    public Estadotarefa buscarPorId(Integer id) { return estadotarefaRepository.findById(id).orElse(null); }
    public Estadotarefa guardar(Estadotarefa et) { return estadotarefaRepository.save(et); }
    public void eliminar(Integer id) { estadotarefaRepository.deleteById(id); }
}