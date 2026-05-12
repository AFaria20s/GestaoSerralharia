package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.services.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    public List<Cargo> listar() {
        return cargoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cargo> buscarPorId(@PathVariable Integer id) {
        Cargo c = cargoService.buscarPorId(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    @GetMapping("/pesquisa")
    public List<Cargo> pesquisar(@RequestParam String nome) {
        return cargoService.buscarPorNome(nome);
    }

    @PostMapping
    public ResponseEntity<Cargo> guardar(@RequestBody Cargo cargo) {
        return ResponseEntity.ok(cargoService.guardar(cargo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cargo> atualizar(@PathVariable Integer id, @RequestBody Cargo cargo) {
        cargo.setId(id);
        return ResponseEntity.ok(cargoService.guardar(cargo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        cargoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}