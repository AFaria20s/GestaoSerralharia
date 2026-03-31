package com.afonso.gestaoSerralharia.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresSequenceSynchronizer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final List<TablePk> TABLES = List.of(
            new TablePk("cargo", "id_cargo"),
            new TablePk("cliente", "id_cliente"),
            new TablePk("codpostal", "id_codpostal"),
            new TablePk("dono", "id_dono"),
            new TablePk("encomenda", "id_encomenda"),
            new TablePk("equipa", "id_equipa"),
            new TablePk("estadoobra", "id_estado_obra"),
            new TablePk("estadopagamento", "id_estado_pagamento"),
            new TablePk("estadotarefa", "id_estado_tarefa"),
            new TablePk("fatura", "id_fatura"),
            new TablePk("fornecedor", "id_fornecedor"),
            new TablePk("funcionario", "id_funcionario"),
            new TablePk("gravidadeproblema", "id_gravidade"),
            new TablePk("linhaencomenda", "id_linha_encomenda"),
            new TablePk("linhaorcamento", "id_linha_orcamento"),
            new TablePk("material", "id_material"),
            new TablePk("obra", "id_obra"),
            new TablePk("orcamento", "id_orcamento"),
            new TablePk("problema", "id_problema"),
            new TablePk("tarefa", "id_tarefa"),
            new TablePk("taxaiva", "id_iva"),
            new TablePk("tipolinhaorcamento", "id_tipo_linhaorcamento"),
            new TablePk("visita", "id_visita")
    );

    @Override
    public void run(ApplicationArguments args) {
        for (TablePk table : TABLES) {
            sincronizar(table);
        }
    }

    private void sincronizar(TablePk table) {
        String sequenceName = jdbcTemplate.queryForObject(
                "select pg_get_serial_sequence(?, ?)",
                String.class,
                table.tableName(),
                table.pkColumn());

        if (sequenceName == null || sequenceName.isBlank()) return;

        String sql = """
                select setval(
                    ?,
                    coalesce((select max(%s) + 1 from %s), 1),
                    false
                )
                """.formatted(table.pkColumn(), table.tableName());

        jdbcTemplate.queryForObject(sql, Long.class, sequenceName);
    }

    private record TablePk(String tableName, String pkColumn) {}
}
