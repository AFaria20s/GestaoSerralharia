package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class EquipafuncionarioId implements Serializable {
    @Column(name = "id_equipa", nullable = false)
    private Integer idEquipa;

    @Column(name = "id_funcionario", nullable = false)
    private Integer idFuncionario;


}