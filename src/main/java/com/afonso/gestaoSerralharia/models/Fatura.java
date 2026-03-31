package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "fatura")
public class Fatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fatura", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_obra")
    private Obra idObra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_pagamento")
    private Estadopagamento idEstadoPagamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_orcamento")
    private Orcamento idOrcamento;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "valor_total_com_iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotalComIva;

    @ColumnDefault("0.00")
    @Column(name = "valor_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "descricao", length = 150)
    private String descricao;

    @ColumnDefault("1")
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

}
