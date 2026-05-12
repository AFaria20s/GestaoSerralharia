package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.services.LinhaorcamentoService;
import com.afonso.gestaoSerralharia.services.OrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/linhas-orcamento")
@RequiredArgsConstructor
public class LinhaorcamentoController {

    private final LinhaorcamentoService linhaorcamentoService;
    private final OrcamentoService orcamentoService;

    @GetMapping
    public List<Linhaorcamento> listar() {
        return linhaorcamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Linhaorcamento> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(linhaorcamentoService.buscarPorId(id));
    }

    @GetMapping("/orcamento/{idOrcamento}")
    public List<Linhaorcamento> buscarPorOrcamento(@PathVariable Integer idOrcamento) {
        Orcamento orcamento = orcamentoService.buscarPorId(idOrcamento);
        return linhaorcamentoService.buscarPorOrcamento(orcamento);
    }

    @PostMapping
    public ResponseEntity<Linhaorcamento> guardar(@RequestBody Linhaorcamento linha) {
        return ResponseEntity.ok(linhaorcamentoService.guardar(linha));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Linhaorcamento> atualizar(@PathVariable Integer id, @RequestBody Linhaorcamento linha) {
        linha.setId(id);
        return ResponseEntity.ok(linhaorcamentoService.guardar(linha));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        linhaorcamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}