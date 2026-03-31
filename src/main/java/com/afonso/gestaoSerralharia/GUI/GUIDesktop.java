package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.windows.AppFrame;
import com.afonso.gestaoSerralharia.services.*;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class GUIDesktop {

    public GUIDesktop(AuthService authService,
                      ObraService obraService,
                      TarefaService tarefaService,
                      FaturaService faturaService,
                      ProblemaService problemaService,
                      ClienteService clienteService,
                      DonoService donoService,
                      FuncionarioService funcionarioService,
                      CargoService cargoService,
                      EstadoobraService estadoobraService,
                      CodpostalService codpostalService,
                      VisitaService visitaService,
                      OrcamentoService orcamentoService,
                      LinhaorcamentoService linhaorcamentoService,
                      TaxaivaService taxaivaService,
                      TipolinhaorcamentoService tipolinhaorcamentoService,
                      GravidadeproblemaService gravidadeproblemaService,
                      EquipafuncionarioService equipafuncionarioService,
                      EquipaService equipaService,
                      EstadopagamentoService estadopagamentoService,
                      MaterialService materialService,
                      FornecedorService fornecedorService,
                      EncomendaService encomendaService,
                      LinhaencomendaService linhaEncomendaService,
                      EstadotarefaService estadotarefaService
    ) {
        setupLookAndFeel();
        new AppFrame(authService, obraService, tarefaService, faturaService,
                problemaService, clienteService, donoService, funcionarioService, cargoService,
                estadoobraService, codpostalService, visitaService, orcamentoService,
                linhaorcamentoService, taxaivaService, tipolinhaorcamentoService, gravidadeproblemaService,
                equipafuncionarioService, equipaService, estadopagamentoService,
                materialService, fornecedorService, encomendaService, linhaEncomendaService,
                estadotarefaService);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
