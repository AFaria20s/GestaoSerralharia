package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Estadopagamento;
import com.afonso.gestaoSerralharia.repositories.EstadopagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadopagamentoService {

    @Autowired
    private EstadopagamentoRepository estadopagamentoRepository;

    public List<Estadopagamento> listarTodos() { return estadopagamentoRepository.findAll(); }
    public Estadopagamento buscarPorId(Integer id) { return estadopagamentoRepository.findById(id).orElse(null); }
    public Estadopagamento guardar(Estadopagamento ep) { return estadopagamentoRepository.save(ep); }
    public void eliminar(Integer id) { estadopagamentoRepository.deleteById(id); }
}
