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

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public Funcionario buscarPorId(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado: " + id));
    }

    public List<Funcionario> buscarPorNome(String nome) {
        return funcionarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Funcionario buscarPorEmail(String email) {
        return funcionarioRepository.findByEmail(email);
    }

    public List<Funcionario> buscarPorCargo(Cargo cargo) {
        return funcionarioRepository.findByIdCargo(cargo);
    }

    public Funcionario guardar(Funcionario funcionario) {
        if (funcionario.getNome() == null || funcionario.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (funcionario.getEmail() == null || !funcionario.getEmail().contains("@"))
            throw new IllegalArgumentException("Email inválido");
        Funcionario existente = funcionarioRepository.findByEmail(funcionario.getEmail());
        if (existente != null && !existente.getId().equals(funcionario.getId()))
            throw new IllegalArgumentException("Já existe um funcionário com este email");
        if (!funcionario.getPassword().startsWith("$2a$"))
            funcionario.setPassword(passwordEncoder.encode(funcionario.getPassword()));
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario autenticar(String email, String passwordPlana) {
        Funcionario func = funcionarioRepository.findByEmail(email);
        if (func == null || !passwordEncoder.matches(passwordPlana, func.getPassword()))
            throw new RuntimeException("Credenciais inválidas");
        return func;
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        funcionarioRepository.deleteById(id);
    }
}
