package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "linhaencomenda")
public class Linhaencomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linha_encomenda", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_encomenda")
    private Encomenda idEncomenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_material")
    private Material idMaterial;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "preco_custo_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCustoUnit;


}