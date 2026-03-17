package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class FuncionariosPanel extends BasePanel {
    public FuncionariosPanel() {
        add(buildHeader("Funcionarios", "RF11 — gerir e criar funcionarios"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
