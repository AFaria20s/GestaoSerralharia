package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class EquipasPanel extends BasePanel {
    public EquipasPanel() {
        add(buildHeader("Equipas", "RF11 — criar, editar e remover equipas"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
