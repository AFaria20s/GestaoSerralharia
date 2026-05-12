package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.services.CargoService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final CargoService cargoService;

    @GetMapping
    public List<Funcionario> listar() {
        return funcionarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionarioService.buscarPorId(id));
    }

    @GetMapping("/pesquisa")
    public List<Funcionario> pesquisar(@RequestParam String nome) {
        return funcionarioService.buscarPorNome(nome);
    }

    @GetMapping("/cargo/{idCargo}")
    public List<Funcionario> buscarPorCargo(@PathVariable Integer idCargo) {
        Cargo cargo = cargoService.buscarPorId(idCargo);
        return funcionarioService.buscarPorCargo(cargo);
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticar(@RequestBody Map<String, String> body) {
        Funcionario f = funcionarioService.autenticar(body.get("email"), body.get("password"));
        if (f == null) return ResponseEntity.status(401).body(Map.of("erro", "Credenciais inválidas"));
        return ResponseEntity.ok(f);
    }

    @PostMapping
    public ResponseEntity<Funcionario> guardar(@RequestBody Funcionario funcionario) {
        return ResponseEntity.ok(funcionarioService.guardar(funcionario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Funcionario> atualizar(@PathVariable Integer id, @RequestBody Funcionario funcionario) {
        funcionario.setId(id);
        return ResponseEntity.ok(funcionarioService.guardar(funcionario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        funcionarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}