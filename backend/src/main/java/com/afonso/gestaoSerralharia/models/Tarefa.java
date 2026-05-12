package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tarefa")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarefa", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_obra")
    private Obra idObra;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_funcionario")
    private Funcionario idFuncionario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_equipa")
    private Equipa idEquipa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado_tarefa")
    private Estadotarefa idEstadoTarefa;

    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;

    @Column(name = "data_limite", nullable = false)
    private LocalDate dataLimite;


}
