package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.windows.AppFrame;
import com.afonso.gestaoSerralharia.services.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;

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
                      EstadotarefaService estadotarefaService,
                      MovimentofinanceiroService movimentofinanceiroService
    ) {
        setupLookAndFeel();
        new AppFrame(authService, obraService, tarefaService, faturaService,
                problemaService, clienteService, donoService, funcionarioService, cargoService,
                estadoobraService, codpostalService, visitaService, orcamentoService,
                linhaorcamentoService, taxaivaService, tipolinhaorcamentoService, gravidadeproblemaService,
                equipafuncionarioService, equipaService, estadopagamentoService,
                materialService, fornecedorService, encomendaService, linhaEncomendaService,
                estadotarefaService, movimentofinanceiroService);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            FlatLaf.setUseNativeWindowDecorations(true);

            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 11);
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("TableHeader.height", 34);
            UIManager.put("TitlePane.unifiedBackground", true);

            UIManager.put("Panel.background", new Color(246, 248, 252));
            UIManager.put("Component.borderColor", new Color(220, 226, 236));
            UIManager.put("Button.background", new Color(255, 255, 255));
            UIManager.put("Button.hoverBackground", new Color(243, 247, 255));
            UIManager.put("Button.focusedBorderColor", new Color(76, 130, 255));
            UIManager.put("Table.selectionBackground", new Color(224, 237, 255));
            UIManager.put("Table.selectionForeground", new Color(25, 45, 82));
            UIManager.put("TableHeader.background", new Color(240, 244, 251));
            UIManager.put("TableHeader.separatorColor", new Color(220, 226, 236));
            UIManager.put("TextField.background", new Color(255, 255, 255));
            UIManager.put("FormattedTextField.background", new Color(255, 255, 255));
            UIManager.put("ComboBox.background", new Color(255, 255, 255));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
