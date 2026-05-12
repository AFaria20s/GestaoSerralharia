package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "equipafuncionario")
public class Equipafuncionario {
    @EmbeddedId
    private EquipafuncionarioId id;

    @MapsId("idEquipa")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_equipa", nullable = false)
    private Equipa idEquipa;

    @MapsId("idFuncionario")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario idFuncionario;


}