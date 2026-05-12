package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "orcamento")
public class Orcamento {

    private static final int PRAZO_PADRAO_DIAS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orcamento", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_obra")
    private Obra idObra;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @ColumnDefault("false")
    @Column(name = "aprovado", nullable = false)
    private Boolean aprovado;

    @ColumnDefault("1")
    @Column(name = "versao", nullable = false)
    private Integer versao;

    @ColumnDefault("true")
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_orcamento_origem")
    private Orcamento idOrcamentoOrigem;

    @ColumnDefault("30")
    @Column(name = "prazo_validade_dias", nullable = false)
    private Integer prazoValidadeDias = PRAZO_PADRAO_DIAS;

    @ColumnDefault("30")
    @Column(name = "prazo_pagamento_dias", nullable = false)
    private Integer prazoPagamentoDias = PRAZO_PADRAO_DIAS;

    @Column(name = "observacoes_financeiras", length = 500)
    private String observacoesFinanceiras;

}
