package com.afonso.gestaoSerralharia.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FinanceiroSeriePonto {
    private LocalDate data;
    private BigDecimal ganhos;
    private BigDecimal perdas;
    private BigDecimal saldo;
    private BigDecimal acumulado;
}
