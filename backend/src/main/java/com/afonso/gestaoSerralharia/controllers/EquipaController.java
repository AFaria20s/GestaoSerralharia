package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Equipafuncionario;
import com.afonso.gestaoSerralharia.models.EquipafuncionarioId;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.services.EquipaService;
import com.afonso.gestaoSerralharia.services.EquipafuncionarioService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import com.afonso.gestaoSerralharia.services.ObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipas")
@RequiredArgsConstructor
public class EquipaController {

    private final EquipaService equipaService;
    private final EquipafuncionarioService equipafuncionarioService;
    private final ObraService obraService;
    private final FuncionarioService funcionarioService;

    @GetMapping
    public List<Equipa> listar() {
        return equipaService.listarTodos();
    }

    @GetMapping("/ativas")
    public List<Equipa> listarAtivas() {
        return equipaService.buscarAtivas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipa> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(equipaService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Equipa> buscarPorObra(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return equipaService.buscarPorObra(obra);
    }

    @PostMapping
    public ResponseEntity<Equipa> guardar(@RequestBody Equipa equipa) {
        return ResponseEntity.ok(equipaService.guardar(equipa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipa> atualizar(@PathVariable Integer id, @RequestBody Equipa equipa) {
        equipa.setId(id);
        return ResponseEntity.ok(equipaService.guardar(equipa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        equipaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Membros da equipa ---

    @GetMapping("/{id}/membros")
    public List<Equipafuncionario> listarMembros(@PathVariable Integer id) {
        Equipa equipa = equipaService.buscarPorId(id);
        return equipafuncionarioService.buscarPorEquipa(equipa);
    }

    @PostMapping("/{idEquipa}/membros/{idFuncionario}")
    public ResponseEntity<Equipafuncionario> adicionarMembro(
            @PathVariable Integer idEquipa,
            @PathVariable Integer idFuncionario) {
        Equipafuncionario ef = new Equipafuncionario();
        ef.setIdEquipa(equipaService.buscarPorId(idEquipa));
        ef.setIdFuncionario(funcionarioService.buscarPorId(idFuncionario));
        return ResponseEntity.ok(equipafuncionarioService.guardar(ef));
    }

    @DeleteMapping("/{idEquipa}/membros/{idFuncionario}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Integer idEquipa,
            @PathVariable Integer idFuncionario) {
        EquipafuncionarioId chave = new EquipafuncionarioId();
        chave.setIdEquipa(idEquipa);
        chave.setIdFuncionario(idFuncionario);
        equipafuncionarioService.eliminar(chave);
        return ResponseEntity.noContent().build();
    }
}