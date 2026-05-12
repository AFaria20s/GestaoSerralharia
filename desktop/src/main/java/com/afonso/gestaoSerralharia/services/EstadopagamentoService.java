package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Estadopagamento;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstadopagamentoService{

    private static final ParameterizedTypeReference<List<Estadopagamento>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EstadopagamentoService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Estadopagamento> listarTodos() {
        return api.get("/api/lookups/estados-pagamento", LIST_TYPE);
    }
    public Estadopagamento buscarPorId(Integer id) {
        return api.get("/api/lookups/estados-pagamento/" + id, Estadopagamento.class);
    }
    public Estadopagamento guardar(Estadopagamento estadopagamento) {
        return api.post("/api/lookups/estados-pagamento", estadopagamento, Estadopagamento.class);
    }
}
