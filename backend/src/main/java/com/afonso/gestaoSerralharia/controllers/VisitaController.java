package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Visita;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.VisitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
@RequiredArgsConstructor
public class VisitaController {

    private final VisitaService visitaService;
    private final ObraService obraService;

    @GetMapping
    public List<Visita> listar() {
        return visitaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visita> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(visitaService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Visita> buscarPorObra(@PathVariable Integer idObra) {
        return visitaService.buscarPorObra(obraService.buscarPorId(idObra));
    }

    @PostMapping
    public ResponseEntity<Visita> guardar(@RequestBody Visita visita) {
        return ResponseEntity.ok(visitaService.guardar(visita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visita> atualizar(@PathVariable Integer id, @RequestBody Visita visita) {
        visita.setId(id);
        return ResponseEntity.ok(visitaService.guardar(visita));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        visitaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}