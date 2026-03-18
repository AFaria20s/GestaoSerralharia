package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class FuncionariosPanel extends BasePanel {
    public FuncionariosPanel() {
        add(buildHeader("Funcionários", "RF11 — gerir e criar funcionários"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
