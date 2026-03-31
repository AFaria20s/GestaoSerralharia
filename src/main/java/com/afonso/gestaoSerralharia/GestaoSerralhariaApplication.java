package com.afonso.gestaoSerralharia;

import com.afonso.gestaoSerralharia.GUI.GUIDesktop;
import com.afonso.gestaoSerralharia.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;

@SpringBootApplication
@RequiredArgsConstructor
public class GestaoSerralhariaApplication implements CommandLineRunner {

    private final ObraService obraService;
    private final TarefaService tarefaService;
    private final FaturaService faturaService;
    private final ProblemaService problemaService;
    private final ClienteService clienteService;
    private final DonoService donoService;
    private final FuncionarioService funcionarioService;
    private final CargoService cargoService;
    private final AuthService authService;
    private final EstadoobraService estadoobraService;
    private final CodpostalService codpostalService;
    private final VisitaService visitaService;
    private final OrcamentoService orcamentoService;
    private final LinhaorcamentoService linhaorcamentoService;
    private final TaxaivaService taxaivaService;
    private final TipolinhaorcamentoService tipolinhaorcamentoService;
    private final GravidadeproblemaService gravidadeproblemaService;
    private final EquipafuncionarioService equipafuncionarioService;
    private final EquipaService equipaService;
    private final EstadopagamentoService estadopagamentoService;
    private final MaterialService materialService;
    private final FornecedorService fornecedorService;
    private final EncomendaService encomendaService;
    private final LinhaencomendaService linhaEncomendaService;
    private final EstadotarefaService estadotarefaService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(GestaoSerralhariaApplication.class)
                .headless(false)
                .run(args);
    }

    @Override
    public void run(String[] args) {
        SwingUtilities.invokeLater(() ->
                new GUIDesktop(authService, obraService, tarefaService, faturaService,
                        problemaService, clienteService, donoService, funcionarioService, cargoService,
                        estadoobraService, codpostalService, visitaService, orcamentoService,
                        linhaorcamentoService, taxaivaService, tipolinhaorcamentoService, gravidadeproblemaService,
                        equipafuncionarioService, equipaService, estadopagamentoService,
                        materialService, fornecedorService, encomendaService, linhaEncomendaService,
                        estadotarefaService
                ));
    }
}
