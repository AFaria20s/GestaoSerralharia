package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));
    }

    public Cliente buscarPorNif(String nif) {
        return clienteRepository.findByNif(nif);
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank())
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        if (cliente.getNif() != null && cliente.getNif().length() != 9)
            throw new IllegalArgumentException("NIF deve ter 9 caracteres");
        if (cliente.getNif() != null) {
            Cliente existente = clienteRepository.findByNif(cliente.getNif());
            if (existente != null && !existente.getId().equals(cliente.getId()))
                throw new IllegalArgumentException("Já existe um cliente com o NIF " + cliente.getNif());
        }
        return clienteRepository.save(cliente);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        clienteRepository.deleteById(id);
    }
}
