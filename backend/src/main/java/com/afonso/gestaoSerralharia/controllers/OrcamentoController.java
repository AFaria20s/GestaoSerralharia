package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.util.TotaisFinanceiros;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.OrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final ObraService obraService;

    @GetMapping
    public List<Orcamento> listar() {
        return orcamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Orcamento> listarPorObra(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return orcamentoService.listarPorObra(obra);
    }

    @GetMapping("/obra/{idObra}/ativo")
    public ResponseEntity<Orcamento> buscarAtivoPorObra(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return orcamentoService.buscarPorObra(obra)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/obra/{idObra}/aprovado")
    public ResponseEntity<Orcamento> buscarAprovadoPorObra(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return orcamentoService.buscarAprovadoPorObra(obra)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aprovados")
    public List<Orcamento> listarAprovados() {
        return orcamentoService.buscarAprovados();
    }

    @GetMapping("/{id}/totais")
    public ResponseEntity<TotaisFinanceiros> calcularTotais(@PathVariable Integer id) {
        Orcamento orcamento = orcamentoService.buscarPorId(id);
        return ResponseEntity.ok(orcamentoService.calcularTotais(orcamento));
    }

    @GetMapping("/{id}/resumo-iva")
    public ResponseEntity<List<ResumoIva>> calcularResumoIva(@PathVariable Integer id) {
        Orcamento orcamento = orcamentoService.buscarPorId(id);
        return ResponseEntity.ok(orcamentoService.calcularResumoIva(orcamento));
    }

    @GetMapping("/{id}/data-validade")
    public ResponseEntity<LocalDate> calcularDataValidade(@PathVariable Integer id) {
        Orcamento orcamento = orcamentoService.buscarPorId(id);
        return ResponseEntity.ok(orcamentoService.calcularDataValidade(orcamento));
    }

    @PostMapping
    public ResponseEntity<Orcamento> guardar(@RequestBody Orcamento orcamento) {
        return ResponseEntity.ok(orcamentoService.guardar(orcamento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orcamento> atualizar(@PathVariable Integer id, @RequestBody Orcamento orcamento) {
        orcamento.setId(id);
        return ResponseEntity.ok(orcamentoService.guardar(orcamento));
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Orcamento> aprovar(@PathVariable Integer id) {
        return ResponseEntity.ok(orcamentoService.aprovar(id));
    }

    @PostMapping("/{id}/revisao")
    public ResponseEntity<Orcamento> criarRevisao(@PathVariable Integer id) {
        return ResponseEntity.ok(orcamentoService.criarRevisao(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        orcamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}