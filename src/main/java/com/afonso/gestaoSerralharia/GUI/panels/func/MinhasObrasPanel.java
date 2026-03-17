package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class MinhasObrasPanel extends BasePanel {
    public MinhasObrasPanel() {
        add(buildHeader("Minhas Obras", "Obras onde participas"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
