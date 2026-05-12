package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Problema;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.ProblemaService;
import com.afonso.gestaoSerralharia.services.TarefaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problemas")
@RequiredArgsConstructor
public class ProblemaController {

    private final ProblemaService problemaService;
    private final ObraService obraService;
    private final TarefaService tarefaService;

    @GetMapping
    public List<Problema> listar() {
        return problemaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problema> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(problemaService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Problema> buscarPorObra(@PathVariable Integer idObra) {
        return problemaService.buscarPorObra(obraService.buscarPorId(idObra));
    }

    @GetMapping("/tarefa/{idTarefa}")
    public List<Problema> buscarPorTarefa(@PathVariable Integer idTarefa) {
        return problemaService.buscarPorTarefa(tarefaService.buscarPorId(idTarefa));
    }

    @PostMapping
    public ResponseEntity<Problema> guardar(@RequestBody Problema problema) {
        return ResponseEntity.ok(problemaService.guardar(problema));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Problema> atualizar(@PathVariable Integer id, @RequestBody Problema problema) {
        problema.setId(id);
        return ResponseEntity.ok(problemaService.guardar(problema));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        problemaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}