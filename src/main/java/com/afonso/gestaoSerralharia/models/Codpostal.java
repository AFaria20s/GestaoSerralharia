package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "codpostal")
public class Codpostal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codpostal", nullable = false)
    private Integer id;

    @Column(name = "codpostal", nullable = false, length = 100)
    private String codpostal;


}