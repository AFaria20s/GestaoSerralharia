package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estadoobra")
public class Estadoobra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_obra", nullable = false)
    private Integer id;

    @Column(name = "nome_estado", nullable = false, length = 20)
    private String nomeEstado;


}