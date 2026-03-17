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
@Table(name = "encomenda")
public class Encomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encomenda", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor idFornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_dono")
    private Dono idDono;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @ColumnDefault("0.00")
    @Column(name = "valor_total_compra", precision = 12, scale = 2)
    private BigDecimal valorTotalCompra;

    @ColumnDefault("false")
    @Column(name = "entregue")
    private Boolean entregue;


}