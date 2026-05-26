package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Movimentofinanceiro;
import com.afonso.gestaoSerralharia.repositories.MovimentofinanceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MovimentofinanceiroService {

    private final MovimentofinanceiroRepository movimentofinanceiroRepository;

    public void registar(LocalDate data, String tipo, String origem, String descricao, BigDecimal valor) {
        if (valor == null || valor.signum() == 0) return;
        Movimentofinanceiro mov = new Movimentofinanceiro();
        mov.setDataMovimento(data != null ? data : LocalDate.now());
        mov.setTipo(tipo != null ? tipo : "OUTRO");
        mov.setOrigem(origem != null ? origem : "SISTEMA");
        mov.setDescricao(descricao);
        mov.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        movimentofinanceiroRepository.save(mov);
    }

    public List<Movimentofinanceiro> listarTodos() {
        return movimentofinanceiroRepository.findAllByOrderByDataMovimentoAscIdAsc();
    }

    public List<Map<String, Object>> serie(int dias) {
        boolean historicoCompleto = dias <= 0;
        int janela = historicoCompleto ? 180 : dias;
        LocalDate inicio = historicoCompleto ? null : LocalDate.now().minusDays(janela - 1L);
        List<Movimentofinanceiro> movimentos = historicoCompleto
                ? movimentofinanceiroRepository.findAllByOrderByDataMovimentoAscIdAsc()
                : movimentofinanceiroRepository.findByDataMovimentoGreaterThanEqualOrderByDataMovimentoAscIdAsc(inicio);

        if (historicoCompleto && !movimentos.isEmpty()) {
            LocalDate primeiraData = movimentos.get(0).getDataMovimento();
            LocalDate hoje = LocalDate.now();
            janela = (int) (hoje.toEpochDay() - primeiraData.toEpochDay()) + 1;
            inicio = primeiraData;
        } else if (historicoCompleto) {
            inicio = LocalDate.now().minusDays(janela - 1L);
        }

        Map<LocalDate, BigDecimal> ganhos = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> perdas = new LinkedHashMap<>();
        for (int i = 0; i < janela; i++) {
            LocalDate d = inicio.plusDays(i);
            ganhos.put(d, BigDecimal.ZERO);
            perdas.put(d, BigDecimal.ZERO);
        }

        for (Movimentofinanceiro m : movimentos) {
            LocalDate d = m.getDataMovimento();
            if (!ganhos.containsKey(d)) continue;
            if (m.getValor().signum() >= 0) ganhos.put(d, ganhos.get(d).add(m.getValor()));
            else perdas.put(d, perdas.get(d).add(m.getValor().abs()));
        }

        List<Map<String, Object>> pontos = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (LocalDate d : ganhos.keySet()) {
            BigDecimal g = ganhos.get(d).setScale(2, RoundingMode.HALF_UP);
            BigDecimal p = perdas.get(d).setScale(2, RoundingMode.HALF_UP);
            BigDecimal saldo = g.subtract(p).setScale(2, RoundingMode.HALF_UP);
            acumulado = acumulado.add(saldo).setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> ponto = new HashMap<>();
            ponto.put("data", d);
            ponto.put("ganhos", g);
            ponto.put("perdas", p);
            ponto.put("saldo", saldo);
            ponto.put("acumulado", acumulado);
            pontos.add(ponto);
        }
        return pontos;
    }
}
