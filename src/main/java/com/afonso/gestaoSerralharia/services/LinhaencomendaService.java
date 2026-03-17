package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Linhaencomenda;
import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.repositories.LinhaencomendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinhaencomendaService {

    @Autowired
    private LinhaencomendaRepository linhaencomendaRepository;

    public List<Linhaencomenda> listarTodos() { return linhaencomendaRepository.findAll(); }
    public Linhaencomenda buscarPorId(Integer id) { return linhaencomendaRepository.findById(id).orElse(null); }
    public List<Linhaencomenda> buscarPorEncomenda(Encomenda encomenda) { return linhaencomendaRepository.findByIdEncomenda(encomenda); }
    public Linhaencomenda guardar(Linhaencomenda linha) { return linhaencomendaRepository.save(linha); }
    public void eliminar(Integer id) { linhaencomendaRepository.deleteById(id); }
}
