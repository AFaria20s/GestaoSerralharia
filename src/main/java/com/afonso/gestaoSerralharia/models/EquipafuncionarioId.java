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
    private static final long serialVersionUID = 3478751780408838200L;
    @Column(name = "id_equipa", nullable = false)
    private Integer idEquipa;

    @Column(name = "id_funcionario", nullable = false)
    private Integer idFuncionario;


}