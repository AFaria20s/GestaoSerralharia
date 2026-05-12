package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Encomenda;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EncomendaService{

    private static final ParameterizedTypeReference<List<Encomenda>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EncomendaService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Encomenda> listarTodos() {
        return api.get("/api/encomendas", LIST_TYPE);
    }
    public Encomenda buscarPorId(Integer id) {
        return api.get("/api/encomendas/" + id, Encomenda.class);
    }
    public List<Encomenda> buscarPorFornecedor(Fornecedor fornecedor) {
        return api.get("/api/encomendas/fornecedor/" + fornecedor.getId(), LIST_TYPE);
    }
    public List<Encomenda> buscarPorEntregue(Boolean entregue) {
        return api.get("/api/encomendas/entregues?entregue=" + entregue, LIST_TYPE);
    }
    public Encomenda guardar(Encomenda encomenda) {
        if (encomenda.getId() == null) {
            return api.post("/api/encomendas", encomenda, Encomenda.class);
        }
        return api.put("/api/encomendas/" + encomenda.getId(), encomenda, Encomenda.class);
    }
    public Encomenda marcarComoEntregue(Integer idEncomenda) {
        return api.post("/api/encomendas/" + idEncomenda + "/entregar", null, Encomenda.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/encomendas/" + id);
    }
}
