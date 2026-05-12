package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.models.Linhaencomenda;
import com.afonso.gestaoSerralharia.services.EncomendaService;
import com.afonso.gestaoSerralharia.services.FornecedorService;
import com.afonso.gestaoSerralharia.services.LinhaencomendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encomendas")
@RequiredArgsConstructor
public class EncomendaController {

    private final EncomendaService encomendaService;
    private final LinhaencomendaService linhaencomendaService;
    private final FornecedorService fornecedorService;

    @GetMapping
    public List<Encomenda> listar() {
        return encomendaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Encomenda> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(encomendaService.buscarPorId(id));
    }

    @GetMapping("/fornecedor/{idFornecedor}")
    public List<Encomenda> buscarPorFornecedor(@PathVariable Integer idFornecedor) {
        return encomendaService.buscarPorFornecedor(fornecedorService.buscarPorId(idFornecedor));
    }

    @GetMapping("/entregues")
    public List<Encomenda> buscarEntregues(@RequestParam(defaultValue = "true") Boolean entregue) {
        return encomendaService.buscarPorEntregue(entregue);
    }

    @GetMapping("/{id}/linhas")
    public List<Linhaencomenda> listarLinhas(@PathVariable Integer id) {
        Encomenda encomenda = encomendaService.buscarPorId(id);
        return linhaencomendaService.buscarPorEncomenda(encomenda);
    }

    @PostMapping
    public ResponseEntity<Encomenda> guardar(@RequestBody Encomenda encomenda) {
        return ResponseEntity.ok(encomendaService.guardar(encomenda));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Encomenda> atualizar(@PathVariable Integer id, @RequestBody Encomenda encomenda) {
        encomenda.setId(id);
        return ResponseEntity.ok(encomendaService.guardar(encomenda));
    }

    @PostMapping("/{id}/entregar")
    public ResponseEntity<Encomenda> marcarComoEntregue(@PathVariable Integer id) {
        return ResponseEntity.ok(encomendaService.marcarComoEntregue(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        encomendaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Linhas ---

    @PostMapping("/{id}/linhas")
    public ResponseEntity<Linhaencomenda> adicionarLinha(@PathVariable Integer id, @RequestBody Linhaencomenda linha) {
        linha.setIdEncomenda(encomendaService.buscarPorId(id));
        return ResponseEntity.ok(linhaencomendaService.guardar(linha));
    }

    @DeleteMapping("/linhas/{idLinha}")
    public ResponseEntity<Void> eliminarLinha(@PathVariable Integer idLinha) {
        linhaencomendaService.eliminar(idLinha);
        return ResponseEntity.noContent().build();
    }
}