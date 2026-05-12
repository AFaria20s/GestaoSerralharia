package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.PagamentoFatura;
import com.afonso.gestaoSerralharia.services.FaturaService;
import com.afonso.gestaoSerralharia.services.ObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faturas")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService faturaService;
    private final ObraService obraService;

    @GetMapping
    public List<Fatura> listar() {
        return faturaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fatura> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(faturaService.buscarPorId(id));
    }

    @GetMapping("/obra/{idObra}")
    public List<Fatura> buscarPorObra(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return faturaService.buscarPorObra(obra);
    }

    @GetMapping("/{id}/pagamentos")
    public ResponseEntity<List<PagamentoFatura>> listarPagamentos(@PathVariable Integer id) {
        Fatura fatura = faturaService.buscarPorId(id);
        return ResponseEntity.ok(faturaService.listarPagamentos(fatura));
    }

    @GetMapping("/{id}/resumo-iva")
    public ResponseEntity<List<ResumoIva>> listarResumoIva(@PathVariable Integer id) {
        Fatura fatura = faturaService.buscarPorId(id);
        return ResponseEntity.ok(faturaService.listarResumoIva(fatura));
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<String> estadoApresentacao(@PathVariable Integer id) {
        Fatura fatura = faturaService.buscarPorId(id);
        return ResponseEntity.ok(faturaService.estadoApresentacao(fatura));
    }

    @GetMapping("/{id}/saldo-divida")
    public ResponseEntity<BigDecimal> saldoEmDivida(@PathVariable Integer id) {
        Fatura fatura = faturaService.buscarPorId(id);
        return ResponseEntity.ok(faturaService.saldoEmDivida(fatura));
    }

    @GetMapping("/obra/{idObra}/total-faturado")
    public ResponseEntity<BigDecimal> totalFaturado(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return ResponseEntity.ok(faturaService.totalFaturado(obra));
    }

    // Emitir fatura total (usa valor do orçamento aprovado)
    @PostMapping("/obra/{idObra}/emitir")
    public ResponseEntity<Fatura> emitir(@PathVariable Integer idObra) {
        Obra obra = obraService.buscarPorId(idObra);
        return ResponseEntity.ok(faturaService.emitir(obra));
    }

    // Emitir fatura parcial com valor e descrição
    @PostMapping("/obra/{idObra}/emitir-parcial")
    public ResponseEntity<Fatura> emitirParcial(
            @PathVariable Integer idObra,
            @RequestBody Map<String, Object> body) {
        Obra obra = obraService.buscarPorId(idObra);
        BigDecimal valor = new BigDecimal(body.get("valor").toString());
        String descricao = body.getOrDefault("descricao", "").toString();
        return ResponseEntity.ok(faturaService.emitir(obra, valor, descricao));
    }

    @PostMapping("/{id}/pagamento")
    public ResponseEntity<Fatura> registarPagamento(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        BigDecimal valor = new BigDecimal(body.get("valor").toString());
        LocalDate data = body.containsKey("data") ? LocalDate.parse(body.get("data").toString()) : LocalDate.now();
        String meio = body.getOrDefault("meio", "Transferência Bancária").toString();
        String ref = body.containsKey("referencia") ? body.get("referencia").toString() : null;
        String obs = body.containsKey("observacoes") ? body.get("observacoes").toString() : null;
        return ResponseEntity.ok(faturaService.registarPagamento(id, valor, data, meio, ref, obs));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        faturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}