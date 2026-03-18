package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class OrcamentosPanel extends BasePanel {
    public OrcamentosPanel() {
        add(buildHeader("Orçamentos", "RF04 · RF05 · RF19"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
