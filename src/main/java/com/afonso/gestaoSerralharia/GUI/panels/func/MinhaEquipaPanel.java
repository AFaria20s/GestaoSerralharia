package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class MinhaEquipaPanel extends BasePanel {
    public MinhaEquipaPanel() {
        add(buildHeader("Minha Equipa", ""), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
