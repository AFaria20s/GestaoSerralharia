package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Estadoobra;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.services.ClienteService;
import com.afonso.gestaoSerralharia.services.EstadoobraService;
import com.afonso.gestaoSerralharia.services.ObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obras")
@RequiredArgsConstructor
public class ObraController {

    private final ObraService obraService;
    private final ClienteService clienteService;
    private final EstadoobraService estadoobraService;

    @GetMapping
    public List<Obra> listar() {
        return obraService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Obra> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(obraService.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    public List<Obra> buscarPorCliente(@PathVariable Integer idCliente) {
        return obraService.buscarPorCliente(clienteService.buscarPorId(idCliente));
    }

    @GetMapping("/estado/{idEstado}")
    public List<Obra> buscarPorEstado(@PathVariable Integer idEstado) {
        Estadoobra estado = estadoobraService.buscarPorId(idEstado);
        return obraService.buscarPorEstado(estado);
    }

    @PostMapping
    public ResponseEntity<Obra> guardar(@RequestBody Obra obra) {
        return ResponseEntity.ok(obraService.guardar(obra));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Obra> atualizar(@PathVariable Integer id, @RequestBody Obra obra) {
        obra.setId(id);
        return ResponseEntity.ok(obraService.guardar(obra));
    }

    @PostMapping("/{id}/estado/{idEstado}")
    public ResponseEntity<Obra> atualizarEstado(@PathVariable Integer id, @PathVariable Integer idEstado) {
        return ResponseEntity.ok(obraService.atualizarEstado(id, idEstado));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<Obra> finalizar(@PathVariable Integer id) {
        return ResponseEntity.ok(obraService.finalizar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        obraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}