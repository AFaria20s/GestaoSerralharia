package com.afonso.gestaoSerralharia;

import com.afonso.gestaoSerralharia.GUI.GUIDesktop;
import com.afonso.gestaoSerralharia.services.AuthService;
import com.afonso.gestaoSerralharia.services.CargoService;
import com.afonso.gestaoSerralharia.services.ClienteService;
import com.afonso.gestaoSerralharia.services.CodpostalService;
import com.afonso.gestaoSerralharia.services.DonoService;
import com.afonso.gestaoSerralharia.services.EncomendaService;
import com.afonso.gestaoSerralharia.services.EquipafuncionarioService;
import com.afonso.gestaoSerralharia.services.EquipaService;
import com.afonso.gestaoSerralharia.services.EstadoobraService;
import com.afonso.gestaoSerralharia.services.EstadopagamentoService;
import com.afonso.gestaoSerralharia.services.EstadotarefaService;
import com.afonso.gestaoSerralharia.services.FaturaService;
import com.afonso.gestaoSerralharia.services.FornecedorService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import com.afonso.gestaoSerralharia.services.GravidadeproblemaService;
import com.afonso.gestaoSerralharia.services.LinhaencomendaService;
import com.afonso.gestaoSerralharia.services.LinhaorcamentoService;
import com.afonso.gestaoSerralharia.services.MaterialService;
import com.afonso.gestaoSerralharia.services.MovimentofinanceiroService;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.OrcamentoService;
import com.afonso.gestaoSerralharia.services.ProblemaService;
import com.afonso.gestaoSerralharia.services.TarefaService;
import com.afonso.gestaoSerralharia.services.TaxaivaService;
import com.afonso.gestaoSerralharia.services.TipolinhaorcamentoService;
import com.afonso.gestaoSerralharia.services.VisitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;

@SpringBootApplication
@RequiredArgsConstructor
public class DesktopApplication implements CommandLineRunner {

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
    private final MovimentofinanceiroService movimentofinanceiroService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(DesktopApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(() ->
                new GUIDesktop(authService, obraService, tarefaService, faturaService,
                        problemaService, clienteService, donoService, funcionarioService, cargoService,
                        estadoobraService, codpostalService, visitaService, orcamentoService,
                        linhaorcamentoService, taxaivaService, tipolinhaorcamentoService, gravidadeproblemaService,
                        equipafuncionarioService, equipaService, estadopagamentoService,
                        materialService, fornecedorService, encomendaService, linhaEncomendaService,
                        estadotarefaService, movimentofinanceiroService));
    }
}
