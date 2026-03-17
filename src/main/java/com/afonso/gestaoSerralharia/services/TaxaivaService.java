package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Taxaiva;
import com.afonso.gestaoSerralharia.repositories.TaxaivaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaxaivaService {

    @Autowired
    private TaxaivaRepository taxaivaRepository;

    public List<Taxaiva> listarTodos() { return taxaivaRepository.findAll(); }
    public Taxaiva buscarPorId(Integer id) { return taxaivaRepository.findById(id).orElse(null); }
    public Taxaiva guardar(Taxaiva taxaiva) { return taxaivaRepository.save(taxaiva); }
    public void eliminar(Integer id) { taxaivaRepository.deleteById(id); }
}
