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
@Table(name = "obra")
public class Obra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obra", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_cliente")
    private Cliente idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @ColumnDefault("1")
    @JoinColumn(name = "id_estado_obra")
    private Estadoobra idEstadoObra;

    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "nporta", nullable = false, length = 20)
    private String nporta;

    @Column(name = "rua", nullable = false, length = 200)
    private String rua;

    @Column(name = "localidade", nullable = false, length = 100)
    private String localidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_codpostal", nullable = false)
    private Codpostal idCodpostal;


}