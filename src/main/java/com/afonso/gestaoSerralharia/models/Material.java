package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor idFornecedor;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @ColumnDefault("0")
    @Column(name = "stock_atual", nullable = false)
    private Integer stockAtual;
}