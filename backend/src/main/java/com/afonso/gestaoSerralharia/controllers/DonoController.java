package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.services.DonoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donos")
@RequiredArgsConstructor
public class DonoController {

    private final DonoService donoService;

    @GetMapping
    public List<Dono> listar() {
        return donoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dono> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(donoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Dono> guardar(@RequestBody Dono dono) {
        return ResponseEntity.ok(donoService.guardar(dono));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dono> atualizar(@PathVariable Integer id, @RequestBody Dono dono) {
        dono.setId(id);
        return ResponseEntity.ok(donoService.guardar(dono));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        donoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}