package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.FinanceiroSeriePonto;
import com.afonso.gestaoSerralharia.models.Movimentofinanceiro;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovimentofinanceiroService {

    private static final ParameterizedTypeReference<List<Movimentofinanceiro>> LIST_MOV = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<FinanceiroSeriePonto>> LIST_SERIE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public MovimentofinanceiroService(DesktopApiSupport api) {
        this.api = api;
    }

    public List<Movimentofinanceiro> listarTodos() {
        return api.get("/api/financeiro/movimentos", LIST_MOV);
    }

    public List<FinanceiroSeriePonto> serie(int dias) {
        return api.get("/api/financeiro/serie?dias=" + dias, LIST_SERIE);
    }
}
