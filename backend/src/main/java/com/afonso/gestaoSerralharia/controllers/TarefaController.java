package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.services.EquipaService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.TarefaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;
    private final ObraService obraService;
    private final FuncionarioService funcionarioService;
    private final EquipaService equipaService;

    @GetMapping
    public List<Tarefa> listar() {
        return tarefaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Tarefa> buscarPorObra(@PathVariable Integer idObra) {
        return tarefaService.buscarPorObra(obraService.buscarPorId(idObra));
    }

    @GetMapping("/funcionario/{idFuncionario}")
    public List<Tarefa> buscarPorFuncionario(@PathVariable Integer idFuncionario) {
        return tarefaService.buscarPorFuncionario(funcionarioService.buscarPorId(idFuncionario));
    }

    @GetMapping("/equipa/{idEquipa}")
    public List<Tarefa> buscarPorEquipa(@PathVariable Integer idEquipa) {
        return tarefaService.buscarPorEquipa(equipaService.buscarPorId(idEquipa));
    }

    @GetMapping("/atrasadas")
    public List<Tarefa> buscarAtrasadas() {
        return tarefaService.buscarAtrasadas();
    }

    @PostMapping
    public ResponseEntity<Tarefa> guardar(@RequestBody Tarefa tarefa) {
        return ResponseEntity.ok(tarefaService.guardar(tarefa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(@PathVariable Integer id, @RequestBody Tarefa tarefa) {
        tarefa.setId(id);
        return ResponseEntity.ok(tarefaService.guardar(tarefa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tarefaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}