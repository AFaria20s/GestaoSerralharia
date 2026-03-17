package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Equipafuncionario;
import com.afonso.gestaoSerralharia.models.EquipafuncionarioId;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.repositories.EquipafuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipafuncionarioService {

    @Autowired
    private EquipafuncionarioRepository equipafuncionarioRepository;

    public List<Equipafuncionario> listarTodos() { return equipafuncionarioRepository.findAll(); }
    public Equipafuncionario buscarPorId(EquipafuncionarioId id) { return equipafuncionarioRepository.findById(id).orElse(null); }
    public List<Equipafuncionario> buscarPorEquipa(Equipa equipa) { return equipafuncionarioRepository.findByIdEquipa(equipa); }
    public List<Equipafuncionario> buscarPorFuncionario(Funcionario funcionario) { return equipafuncionarioRepository.findByIdFuncionario(funcionario); }
    public Equipafuncionario guardar(Equipafuncionario ef) { return equipafuncionarioRepository.save(ef); }
    public void eliminar(EquipafuncionarioId id) { equipafuncionarioRepository.deleteById(id); }
}
