package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.repositories.DonoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonoService {

    private final DonoRepository donoRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Dono> listarTodos() {
        return donoRepository.findAll();
    }

    public Dono buscarPorId(Integer id) {
        return donoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dono não encontrado: " + id));
    }

    public Dono buscarPorEmail(String email) {
        return donoRepository.findByEmail(email);
    }

    public Dono guardar(Dono dono) {
        if (dono.getNome() == null || dono.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (dono.getEmail() == null || !dono.getEmail().contains("@"))
            throw new IllegalArgumentException("Email inválido");
        if (donoRepository.findByEmail(dono.getEmail()) != null && dono.getId() == null)
            throw new IllegalArgumentException("Já existe uma conta com este email");
        if (!dono.getPassword().startsWith("$2a$"))
            dono.setPassword(passwordEncoder.encode(dono.getPassword()));
        return donoRepository.save(dono);
    }

    public Dono autenticar(String email, String passwordPlana) {
        Dono dono = donoRepository.findByEmail(email);
        if (dono == null || !passwordEncoder.matches(passwordPlana, dono.getPassword()))
            throw new RuntimeException("Credenciais inválidas");
        return dono;
    }

    public void eliminar(Integer id) {
        donoRepository.deleteById(id);
    }
}
