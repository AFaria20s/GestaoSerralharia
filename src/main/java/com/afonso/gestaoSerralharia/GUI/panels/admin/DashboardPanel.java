package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class DashboardPanel extends BasePanel {
    public DashboardPanel() {
        add(buildHeader("Dashboard", "Resumo geral da empresa"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
