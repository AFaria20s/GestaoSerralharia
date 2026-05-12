package com.afonso.gestaoSerralharia.util;

import java.math.BigDecimal;

public record TotaisFinanceiros(
        BigDecimal subtotalSemIva,
        BigDecimal valorDesconto,
        BigDecimal valorIva,
        BigDecimal totalComIva
) {
}
