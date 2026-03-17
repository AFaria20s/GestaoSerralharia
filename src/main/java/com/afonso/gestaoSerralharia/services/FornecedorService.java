package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.repositories.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    public List<Fornecedor> listarTodos() { return fornecedorRepository.findAll(); }
    public Fornecedor buscarPorId(Integer id) { return fornecedorRepository.findById(id).orElse(null); }
    public List<Fornecedor> buscarPorNome(String nome) { return fornecedorRepository.findByNomeContainingIgnoreCase(nome); }
    public Fornecedor buscarPorNif(String nif) { return fornecedorRepository.findByNif(nif); }
    public Fornecedor buscarPorEmail(String email) { return fornecedorRepository.findByEmail(email); }
    public Fornecedor guardar(Fornecedor fornecedor) { return fornecedorRepository.save(fornecedor); }
    public void eliminar(Integer id) { fornecedorRepository.deleteById(id); }
}
