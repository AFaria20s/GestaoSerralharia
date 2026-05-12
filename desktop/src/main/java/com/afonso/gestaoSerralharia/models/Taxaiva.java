package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "taxaiva")
public class Taxaiva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_iva", nullable = false)
    private Integer id;

    @Column(name = "percentagem", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentagem;

    @Column(name = "descricao", nullable = false, length = 50)
    private String descricao;


}