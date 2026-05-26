package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "movimentofinanceiro")
public class Movimentofinanceiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimento", nullable = false)
    private Integer id;

    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "origem", nullable = false, length = 30)
    private String origem;

    @Column(name = "descricao", length = 250)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
}
