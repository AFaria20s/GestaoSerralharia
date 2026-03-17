package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class ObrasPanel extends BasePanel {
    public ObrasPanel() {
        add(buildHeader("Obras", "RF02 · RF07 · RF08 · RF20 · RF22"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
