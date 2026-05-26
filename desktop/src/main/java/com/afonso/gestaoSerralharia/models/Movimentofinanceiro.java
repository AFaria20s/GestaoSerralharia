package com.afonso.gestaoSerralharia.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class Movimentofinanceiro {
    private Integer id;
    private LocalDate dataMovimento;
    private String tipo;
    private String origem;
    private String descricao;
    private BigDecimal valor;
}
