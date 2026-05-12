package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.models.Material;
import com.afonso.gestaoSerralharia.services.FornecedorService;
import com.afonso.gestaoSerralharia.services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiais")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final FornecedorService fornecedorService;

    @GetMapping
    public List<Material> listar() {
        return materialService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(materialService.buscarPorId(id));
    }

    @GetMapping("/pesquisa")
    public List<Material> pesquisar(@RequestParam String nome) {
        return materialService.buscarPorNome(nome);
    }

    @GetMapping("/fornecedor/{idFornecedor}")
    public List<Material> buscarPorFornecedor(@PathVariable Integer idFornecedor) {
        Fornecedor f = fornecedorService.buscarPorId(idFornecedor);
        return materialService.buscarPorFornecedor(f);
    }

    @GetMapping("/stock-baixo")
    public List<Material> buscarStockBaixo(@RequestParam(defaultValue = "5") Integer limite) {
        return materialService.buscarStockBaixo(limite);
    }

    @GetMapping("/{id}/stock-disponivel")
    public ResponseEntity<BigDecimal> stockDisponivel(@PathVariable Integer id) {
        Material m = materialService.buscarPorId(id);
        return ResponseEntity.ok(materialService.stockDisponivel(m));
    }

    @PostMapping
    public ResponseEntity<Material> guardar(@RequestBody Material material) {
        return ResponseEntity.ok(materialService.guardar(material));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> atualizar(@PathVariable Integer id, @RequestBody Material material) {
        material.setId(id);
        return ResponseEntity.ok(materialService.guardar(material));
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<Void> reservar(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Material m = materialService.buscarPorId(id);
        BigDecimal quantidade = new BigDecimal(body.get("quantidade").toString());
        materialService.reservar(m, quantidade);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/libertar-reserva")
    public ResponseEntity<Void> libertarReserva(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Material m = materialService.buscarPorId(id);
        BigDecimal quantidade = new BigDecimal(body.get("quantidade").toString());
        materialService.libertarReserva(m, quantidade);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        materialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}