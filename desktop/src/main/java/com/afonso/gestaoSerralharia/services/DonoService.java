package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Dono;
import org.springframework.stereotype.Component;

@Component
public class DonoService{

    private final DesktopApiSupport api;

    public DonoService(DesktopApiSupport api) {
        this.api = api;
    }
    public Dono buscarPorId(Integer id) {
        return api.get("/api/donos/" + id, Dono.class);
    }
    public Dono guardar(Dono dono) {
        if (dono.getId() == null) {
            return api.post("/api/donos", dono, Dono.class);
        }
        return api.put("/api/donos/" + dono.getId(), dono, Dono.class);
    }
    public void eliminar(Integer id) {
        api.delete("/api/donos/" + id);
    }
}
