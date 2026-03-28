package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;
import com.afonso.gestaoSerralharia.GUI.utils.AppConfig;
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
                      EquipaService equipaService
    ) {
        setupLookAndFeel();
        new AppFrame(authService, obraService, tarefaService, faturaService,
                problemaService, clienteService, funcionarioService, cargoService,
                estadoobraService, codpostalService, visitaService, orcamentoService,
                linhaorcamentoService, taxaivaService, tipolinhaorcamentoService, gravidadeproblemaService,
                equipafuncionarioService, equipaService);
    }

    private void setupLookAndFeel() {
        try {
            ThemeType theme = AppConfig.getInstance().getTheme();
            if (theme == ThemeType.LIGHT)
                UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
