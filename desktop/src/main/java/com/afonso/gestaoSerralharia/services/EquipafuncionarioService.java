package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import com.afonso.gestaoSerralharia.models.Equipafuncionario;
import com.afonso.gestaoSerralharia.models.EquipafuncionarioId;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Funcionario;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipafuncionarioService{

    private static final ParameterizedTypeReference<List<Equipafuncionario>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final DesktopApiSupport api;

    public EquipafuncionarioService(DesktopApiSupport api) {
        this.api = api;
    }
    public Equipafuncionario buscarPorId(EquipafuncionarioId id) {
        return buscarPorEquipaId(id.getIdEquipa()).stream()
                .filter(item -> item.getIdFuncionario() != null
                        && item.getIdFuncionario().getId() != null
                        && item.getIdFuncionario().getId().equals(id.getIdFuncionario()))
                .findFirst()
                .orElse(null);
    }
    public List<Equipafuncionario> buscarPorEquipa(Equipa equipa) {
        return buscarPorEquipaId(equipa.getId());
    }
    public Equipafuncionario guardar(Equipafuncionario ef) {
        api.post("/api/equipas/" + ef.getIdEquipa().getId() + "/membros/" + ef.getIdFuncionario().getId(),
                null, Void.class);
        return ef;
    }
    public void eliminar(EquipafuncionarioId id) {
        api.delete("/api/equipas/" + id.getIdEquipa() + "/membros/" + id.getIdFuncionario());
    }
    public boolean pertenceAEquipa(Equipa equipa, Funcionario funcionario) {
        return buscarPorEquipa(equipa).stream()
                .anyMatch(ef -> ef.getIdFuncionario() != null
                        && ef.getIdFuncionario().getId() != null
                        && ef.getIdFuncionario().getId().equals(funcionario.getId()));
    }

    private List<Equipafuncionario> buscarPorEquipaId(Integer idEquipa) {
        return api.get("/api/equipas/" + idEquipa + "/membros", LIST_TYPE);
    }
}
