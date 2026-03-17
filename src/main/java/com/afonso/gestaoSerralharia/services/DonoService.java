package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.repositories.DonoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonoService {

    @Autowired
    private DonoRepository donoRepository;

    public List<Dono> listarTodos() { return donoRepository.findAll(); }
    public Dono buscarPorId(Integer id) { return donoRepository.findById(id).orElse(null); }
    public Dono buscarPorEmail(String email) { return donoRepository.findByEmail(email); }
    public Dono guardar(Dono dono) { return donoRepository.save(dono); }
    public void eliminar(Integer id) { donoRepository.deleteById(id); }
}
