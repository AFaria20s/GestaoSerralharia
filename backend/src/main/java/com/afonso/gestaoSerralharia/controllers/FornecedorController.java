package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.services.FornecedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<Fornecedor> listar() {
        return fornecedorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> buscarPorId(@PathVariable Integer id) {
        Fornecedor f = fornecedorService.buscarPorId(id);
        return f != null ? ResponseEntity.ok(f) : ResponseEntity.notFound().build();
    }

    @GetMapping("/pesquisa")
    public List<Fornecedor> pesquisar(@RequestParam String nome) {
        return fornecedorService.buscarPorNome(nome);
    }

    @GetMapping("/nif/{nif}")
    public ResponseEntity<Fornecedor> buscarPorNif(@PathVariable String nif) {
        Fornecedor f = fornecedorService.buscarPorNif(nif);
        return f != null ? ResponseEntity.ok(f) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Fornecedor> guardar(@RequestBody Fornecedor fornecedor) {
        return ResponseEntity.ok(fornecedorService.guardar(fornecedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fornecedor> atualizar(@PathVariable Integer id, @RequestBody Fornecedor fornecedor) {
        fornecedor.setId(id);
        return ResponseEntity.ok(fornecedorService.guardar(fornecedor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        fornecedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}