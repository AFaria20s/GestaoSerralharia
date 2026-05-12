package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller único para todas as tabelas de lookup (estados, taxas, tipos).
 * Estas entidades são maioritariamente read-only em runtime.
 */
@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
public class LookupsController {

    private final EstadoobraService estadoobraService;
    private final EstadopagamentoService estadopagamentoService;
    private final EstadotarefaService estadotarefaService;
    private final GravidadeproblemaService gravidadeproblemaService;
    private final TaxaivaService taxaivaService;
    private final TipolinhaorcamentoService tipolinhaorcamentoService;
    private final CodpostalService codpostalService;

    // --- Estados de Obra ---
    @GetMapping("/estados-obra")
    public List<Estadoobra> estadosObra() { return estadoobraService.listarTodos(); }

    @GetMapping("/estados-obra/{id}")
    public ResponseEntity<Estadoobra> estadoObra(@PathVariable Integer id) {
        Estadoobra e = estadoobraService.buscarPorId(id);
        return e != null ? ResponseEntity.ok(e) : ResponseEntity.notFound().build();
    }

    @PostMapping("/estados-obra")
    public ResponseEntity<Estadoobra> guardarEstadoObra(@RequestBody Estadoobra e) {
        return ResponseEntity.ok(estadoobraService.guardar(e));
    }

    @DeleteMapping("/estados-obra/{id}")
    public ResponseEntity<Void> eliminarEstadoObra(@PathVariable Integer id) {
        estadoobraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Estados de Pagamento ---
    @GetMapping("/estados-pagamento")
    public List<Estadopagamento> estadosPagamento() { return estadopagamentoService.listarTodos(); }

    @GetMapping("/estados-pagamento/{id}")
    public ResponseEntity<Estadopagamento> estadoPagamento(@PathVariable Integer id) {
        Estadopagamento e = estadopagamentoService.buscarPorId(id);
        return e != null ? ResponseEntity.ok(e) : ResponseEntity.notFound().build();
    }

    @PostMapping("/estados-pagamento")
    public ResponseEntity<Estadopagamento> guardarEstadoPagamento(@RequestBody Estadopagamento e) {
        return ResponseEntity.ok(estadopagamentoService.guardar(e));
    }

    // --- Estados de Tarefa ---
    @GetMapping("/estados-tarefa")
    public List<Estadotarefa> estadosTarefa() { return estadotarefaService.listarTodos(); }

    @GetMapping("/estados-tarefa/{id}")
    public ResponseEntity<Estadotarefa> estadoTarefa(@PathVariable Integer id) {
        Estadotarefa e = estadotarefaService.buscarPorId(id);
        return e != null ? ResponseEntity.ok(e) : ResponseEntity.notFound().build();
    }

    @PostMapping("/estados-tarefa")
    public ResponseEntity<Estadotarefa> guardarEstadoTarefa(@RequestBody Estadotarefa e) {
        return ResponseEntity.ok(estadotarefaService.guardar(e));
    }

    // --- Gravidade Problema ---
    @GetMapping("/gravidades-problema")
    public List<Gravidadeproblema> gravidadesProblema() { return gravidadeproblemaService.listarTodos(); }

    @GetMapping("/gravidades-problema/{id}")
    public ResponseEntity<Gravidadeproblema> gravidadeProblema(@PathVariable Integer id) {
        Gravidadeproblema g = gravidadeproblemaService.buscarPorId(id);
        return g != null ? ResponseEntity.ok(g) : ResponseEntity.notFound().build();
    }

    @PostMapping("/gravidades-problema")
    public ResponseEntity<Gravidadeproblema> guardarGravidade(@RequestBody Gravidadeproblema g) {
        return ResponseEntity.ok(gravidadeproblemaService.guardar(g));
    }

    // --- Taxas IVA ---
    @GetMapping("/taxas-iva")
    public List<Taxaiva> taxasIva() { return taxaivaService.listarTodos(); }

    @GetMapping("/taxas-iva/{id}")
    public ResponseEntity<Taxaiva> taxaIva(@PathVariable Integer id) {
        Taxaiva t = taxaivaService.buscarPorId(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    @PostMapping("/taxas-iva")
    public ResponseEntity<Taxaiva> guardarTaxaIva(@RequestBody Taxaiva t) {
        return ResponseEntity.ok(taxaivaService.guardar(t));
    }

    @DeleteMapping("/taxas-iva/{id}")
    public ResponseEntity<Void> eliminarTaxaIva(@PathVariable Integer id) {
        taxaivaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Tipos de Linha de Orçamento ---
    @GetMapping("/tipos-linha-orcamento")
    public List<Tipolinhaorcamento> tiposLinhaOrcamento() { return tipolinhaorcamentoService.listarTodos(); }

    @GetMapping("/tipos-linha-orcamento/{id}")
    public ResponseEntity<Tipolinhaorcamento> tipoLinhaOrcamento(@PathVariable Integer id) {
        Tipolinhaorcamento t = tipolinhaorcamentoService.buscarPorId(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    @PostMapping("/tipos-linha-orcamento")
    public ResponseEntity<Tipolinhaorcamento> guardarTipoLinha(@RequestBody Tipolinhaorcamento t) {
        return ResponseEntity.ok(tipolinhaorcamentoService.guardar(t));
    }

    // --- Códigos Postais ---
    @GetMapping("/codpostais")
    public List<Codpostal> codpostais() { return codpostalService.listarTodos(); }

    @GetMapping("/codpostais/pesquisa")
    public List<Codpostal> pesquisarCodpostal(@RequestParam String q) {
        return codpostalService.buscarPorCodpostal(q);
    }

    @GetMapping("/codpostais/{id}")
    public ResponseEntity<Codpostal> codpostal(@PathVariable Integer id) {
        Codpostal cp = codpostalService.buscarPorId(id);
        return cp != null ? ResponseEntity.ok(cp) : ResponseEntity.notFound().build();
    }

    @PostMapping("/codpostais/encontrar-ou-criar")
    public ResponseEntity<Codpostal> encontrarOuCriar(@RequestBody String texto) {
        return ResponseEntity.ok(codpostalService.encontrarOuCriar(texto));
    }

    @PostMapping("/codpostais")
    public ResponseEntity<Codpostal> guardarCodpostal(@RequestBody Codpostal cp) {
        return ResponseEntity.ok(codpostalService.guardar(cp));
    }

    @DeleteMapping("/codpostais/{id}")
    public ResponseEntity<Void> eliminarCodpostal(@PathVariable Integer id) {
        codpostalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
