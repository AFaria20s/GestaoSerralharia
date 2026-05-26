package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.models.Movimentofinanceiro;
import com.afonso.gestaoSerralharia.services.MovimentofinanceiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final MovimentofinanceiroService movimentofinanceiroService;

    @GetMapping("/movimentos")
    public List<Movimentofinanceiro> movimentos() {
        return movimentofinanceiroService.listarTodos();
    }

    @GetMapping("/serie")
    public List<Map<String, Object>> serie(@RequestParam(defaultValue = "180") Integer dias) {
        return movimentofinanceiroService.serie(dias != null ? dias : 180);
    }
}
