package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class VisitasPanel extends BasePanel {
    public VisitasPanel() {
        add(buildHeader("Visitas", "RF03 · RF18 — agendar visitas e medicoes"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
