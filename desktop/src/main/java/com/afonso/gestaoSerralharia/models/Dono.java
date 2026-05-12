package com.afonso.gestaoSerralharia.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dono")
public class Dono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dono", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "imagem_perfil", length = 500)
    private String imagemPerfil;

    @Column(name = "painel_inicial", length = 40)
    private String painelInicial;

    // ── Dados da empresa (usados no PDF do orçamento) ──────────────────────

    @Column(name = "empresa_nome", length = 150)
    private String empresaNome;

    @Column(name = "empresa_morada", length = 250)
    private String empresaMorada;

    @Column(name = "empresa_nif", length = 20)
    private String empresaNif;

    @Column(name = "empresa_telefone", length = 30)
    private String empresaTelefone;

    @Column(name = "empresa_email", length = 100)
    private String empresaEmail;

    @Column(name = "empresa_iban", length = 60)
    private String empresaIban;

}