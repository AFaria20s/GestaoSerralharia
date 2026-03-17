package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fornecedor")
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fornecedor", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "nif", nullable = false, length = 9)
    private String nif;

    @Column(name = "contacto", nullable = false, length = 50)
    private String contacto;


}