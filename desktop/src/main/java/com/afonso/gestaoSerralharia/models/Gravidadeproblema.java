package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gravidadeproblema")
public class Gravidadeproblema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gravidade", nullable = false)
    private Integer id;

    @Column(name = "nome_gravidade", nullable = false, length = 20)
    private String nomeGravidade;


}