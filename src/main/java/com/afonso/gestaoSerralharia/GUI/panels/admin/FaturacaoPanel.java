package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class FaturacaoPanel extends BasePanel {
    public FaturacaoPanel() {
        add(buildHeader("Faturação", "RF09 · RF10"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
