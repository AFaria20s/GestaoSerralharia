package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.repositories.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CargoService {
    @Autowired
    private CargoRepository cargoRepository;

    public List<Cargo> listarTodos() {
        return cargoRepository.findAll();
    }
    public Cargo buscarPorId(Integer id) {
        return cargoRepository.findById(id).orElse(null);
    }
    public List<Cargo> buscarPorNome(String nome) {
        return cargoRepository.findByNomeContainingIgnoreCase(nome);
    }
    public Cargo guardar(Cargo cargo) { return cargoRepository.save(cargo); }
    public void eliminar(Integer id) { cargoRepository.deleteById(id); }
}
