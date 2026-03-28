package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "linhaorcamento")
public class Linhaorcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linha_orcamento", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_orcamento")
    private Orcamento idOrcamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_material")
    private Material idMaterial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_linhaorcamento")
    private Tipolinhaorcamento idTipoLinhaorcamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_iva")
    private Taxaiva idIva;

    @Column(name = "iva_percentagem_aplicada", nullable = false, precision = 5, scale = 2)
    private BigDecimal ivaPercentagemAplicada;

    @ColumnDefault("1")
    @Column(name = "quantidade", precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "preco_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnit;

    @Column(name = "nome", length = 150)
    private String nome;


}