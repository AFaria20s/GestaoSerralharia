package com.afonso.gestaoSerralharia.util;

import java.math.BigDecimal;

public record ResumoIva(
        BigDecimal taxaPercentagem,
        BigDecimal baseTributavel,
        BigDecimal valorIva,
        BigDecimal totalComIva
) {
}
