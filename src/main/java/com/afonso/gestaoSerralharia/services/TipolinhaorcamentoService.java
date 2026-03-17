package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Tipolinhaorcamento;
import com.afonso.gestaoSerralharia.repositories.TipolinhaorcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipolinhaorcamentoService {

    @Autowired
    private TipolinhaorcamentoRepository tipolinhaorcamentoRepository;

    public List<Tipolinhaorcamento> listarTodos() { return tipolinhaorcamentoRepository.findAll(); }
    public Tipolinhaorcamento buscarPorId(Integer id) { return tipolinhaorcamentoRepository.findById(id).orElse(null); }
    public Tipolinhaorcamento guardar(Tipolinhaorcamento t) { return tipolinhaorcamentoRepository.save(t); }
    public void eliminar(Integer id) { tipolinhaorcamentoRepository.deleteById(id); }
}
