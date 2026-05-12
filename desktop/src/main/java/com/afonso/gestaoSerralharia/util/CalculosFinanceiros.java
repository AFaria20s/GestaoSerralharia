package com.afonso.gestaoSerralharia.util;

import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.models.Codpostal;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Obra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CalculosFinanceiros {

    private static final BigDecimal CEM = new BigDecimal("100");

    private CalculosFinanceiros() {}

    public static BigDecimal zeroIfNull(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    public static BigDecimal normalizarDinheiro(BigDecimal valor) {
        return zeroIfNull(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentagem(BigDecimal valor) {
        return zeroIfNull(valor).divide(CEM, 6, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtotalBruto(Linhaorcamento linha) {
        BigDecimal quantidade = zeroIfNull(linha.getQuantidade());
        BigDecimal precoUnit = zeroIfNull(linha.getPrecoUnit());
        return normalizarDinheiro(quantidade.multiply(precoUnit));
    }

    public static BigDecimal valorDesconto(Linhaorcamento linha) {
        BigDecimal bruto = subtotalBruto(linha);
        BigDecimal desconto = bruto.multiply(percentagem(linha.getDescontoPercentagem()));
        return normalizarDinheiro(desconto);
    }

    public static BigDecimal subtotalSemIva(Linhaorcamento linha) {
        BigDecimal liquido = subtotalBruto(linha).subtract(valorDesconto(linha));
        return normalizarDinheiro(liquido.max(BigDecimal.ZERO));
    }

    public static BigDecimal valorIva(Linhaorcamento linha) {
        BigDecimal iva = subtotalSemIva(linha).multiply(percentagem(linha.getIvaPercentagemAplicada()));
        return normalizarDinheiro(iva);
    }

    public static BigDecimal totalComIva(Linhaorcamento linha) {
        return normalizarDinheiro(subtotalSemIva(linha).add(valorIva(linha)));
    }

    public static TotaisFinanceiros totaisLinhas(Collection<Linhaorcamento> linhas) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal desconto = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Linhaorcamento linha : linhas) {
            subtotal = subtotal.add(subtotalBruto(linha));
            desconto = desconto.add(valorDesconto(linha));
            iva = iva.add(valorIva(linha));
            total = total.add(totalComIva(linha));
        }

        return new TotaisFinanceiros(
                normalizarDinheiro(subtotal),
                normalizarDinheiro(desconto),
                normalizarDinheiro(iva),
                normalizarDinheiro(total)
        );
    }

    public static List<ResumoIva> resumoIva(Collection<Linhaorcamento> linhas) {
        Map<BigDecimal, BigDecimal[]> acumulado = new LinkedHashMap<>();

        for (Linhaorcamento linha : linhas) {
            BigDecimal taxa = normalizarTaxa(linha.getIvaPercentagemAplicada());
            BigDecimal[] valores = acumulado.computeIfAbsent(taxa,
                    ignored -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            valores[0] = valores[0].add(subtotalSemIva(linha));
            valores[1] = valores[1].add(valorIva(linha));
            valores[2] = valores[2].add(totalComIva(linha));
        }

        return acumulado.entrySet().stream()
                .map(entry -> new ResumoIva(
                        entry.getKey(),
                        normalizarDinheiro(entry.getValue()[0]),
                        normalizarDinheiro(entry.getValue()[1]),
                        normalizarDinheiro(entry.getValue()[2])))
                .toList();
    }

    public static BigDecimal normalizarTaxa(BigDecimal taxa) {
        return zeroIfNull(taxa).setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatarMoradaCliente(Cliente cliente) {
        if (cliente == null) return "";
        StringBuilder sb = new StringBuilder();
        append(sb, cliente.getRua());
        if (cliente.getNporta() != null && !cliente.getNporta().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("nº ").append(cliente.getNporta().trim());
        }
        Codpostal codpostal = cliente.getIdCodpostal();
        if (codpostal != null && codpostal.getCodpostal() != null && !codpostal.getCodpostal().isBlank()) {
            if (!sb.isEmpty()) sb.append(" - ");
            sb.append(codpostal.getCodpostal().trim());
        }
        return sb.toString();
    }

    public static String formatarMoradaObra(Obra obra) {
        if (obra == null) return "";
        StringBuilder sb = new StringBuilder();
        append(sb, obra.getRua());
        if (obra.getNporta() != null && !obra.getNporta().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("nº ").append(obra.getNporta().trim());
        }
        appendWithSeparator(sb, obra.getLocalidade(), " - ");
        if (obra.getIdCodpostal() != null) {
            appendWithSeparator(sb, obra.getIdCodpostal().getCodpostal(), " ");
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, String texto) {
        if (texto == null || texto.isBlank()) return;
        if (!sb.isEmpty()) sb.append(", ");
        sb.append(texto.trim());
    }

    private static void appendWithSeparator(StringBuilder sb, String texto, String separador) {
        if (texto == null || texto.isBlank()) return;
        if (!sb.isEmpty()) sb.append(separador);
        sb.append(texto.trim());
    }
}
