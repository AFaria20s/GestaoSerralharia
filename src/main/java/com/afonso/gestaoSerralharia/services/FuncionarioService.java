package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.repositories.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Funcionario> listarTodos()          { return funcionarioRepository.findAll(); }
    public Funcionario buscarPorEmail(String email)  { return funcionarioRepository.findByEmail(email); }

    public Funcionario buscarPorId(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado: " + id));
    }

    public List<Funcionario> buscarPorNome(String nome) {
        return funcionarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Funcionario> buscarPorCargo(Cargo cargo) {
        return funcionarioRepository.findByIdCargo(cargo);
    }

    public Funcionario guardar(Funcionario f) {
        if (f.getNome() == null || f.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (f.getEmail() == null || !f.getEmail().contains("@"))
            throw new IllegalArgumentException("Email inválido");
        Funcionario existente = funcionarioRepository.findByEmail(f.getEmail());
        if (existente != null && !existente.getId().equals(f.getId()))
            throw new IllegalArgumentException("Já existe um funcionário com este email");
        if (!f.getPassword().startsWith("$2a$"))
            f.setPassword(passwordEncoder.encode(f.getPassword()));
        return funcionarioRepository.save(f);
    }

    public Funcionario autenticar(String email, String passwordPlana) {
        Funcionario f = funcionarioRepository.findByEmail(email);
        if (f != null && passwordEncoder.matches(passwordPlana, f.getPassword()))
            return f;
        return null;
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        funcionarioRepository.deleteById(id);
    }
}
