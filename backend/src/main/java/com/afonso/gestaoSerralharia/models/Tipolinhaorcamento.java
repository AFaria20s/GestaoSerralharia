package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipolinhaorcamento")
public class Tipolinhaorcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_linhaorcamento", nullable = false)
    private Integer id;

    @Column(name = "nome_tipo", nullable = false, length = 30)
    private String nomeTipo;


}