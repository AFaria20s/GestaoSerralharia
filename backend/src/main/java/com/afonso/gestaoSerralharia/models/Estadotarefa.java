package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estadotarefa")
public class Estadotarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_tarefa", nullable = false)
    private Integer id;

    @Column(name = "nome_estado", nullable = false, length = 20)
    private String nomeEstado;


}