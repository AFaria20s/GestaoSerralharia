package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class ReportarProblemaPanel extends BasePanel {
    public ReportarProblemaPanel() {
        add(buildHeader("Reportar Problema", "RF13"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
