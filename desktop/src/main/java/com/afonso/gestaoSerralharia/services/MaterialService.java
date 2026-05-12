package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Fornecedor;
import com.afonso.gestaoSerralharia.models.Material;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MaterialService{

    private static final ParameterizedTypeReference<List<Material>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public MaterialService(DesktopApiSupport api) {
        this.api = api;
    }
    public List<Material> listarTodos() {
        return api.get("/api/materiais", LIST_TYPE);
    }
    public Material buscarPorId(Integer id) {
        return api.get("/api/materiais/" + id, Material.class);
    }
    public List<Material> buscarPorNome(String nome) {
        return api.get("/api/materiais/pesquisa?q=" + api.encode(nome), LIST_TYPE);
    }
    public List<Material> buscarPorFornecedor(Fornecedor fornecedor) {
        return api.get("/api/materiais/fornecedor/" + fornecedor.getId(), LIST_TYPE);
    }
    public List<Material> buscarStockBaixo(Integer limite) {
        return api.get("/api/materiais/stock-baixo?limite=" + limite, LIST_TYPE);
    }
    public Material guardar(Material material) {
        if (material.getId() == null) {
            return api.post("/api/materiais", material, Material.class);
        }
        return api.put("/api/materiais/" + material.getId(), material, Material.class);
    }
    public BigDecimal stockReservado(Material material) {
        return material != null && material.getStockReservado() != null ? material.getStockReservado() : BigDecimal.ZERO;
    }
    public BigDecimal stockDisponivel(Material material) {
        if (material == null || material.getId() == null) {
            return BigDecimal.ZERO;
        }
        return api.get("/api/materiais/" + material.getId() + "/stock-disponivel", BigDecimal.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/materiais/" + id);
    }
}
