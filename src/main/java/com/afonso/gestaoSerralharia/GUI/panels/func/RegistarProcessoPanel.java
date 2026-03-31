package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class RegistarProcessoPanel extends BasePanel {
    public RegistarProcessoPanel() {
        add(buildHeader("Registar Processo", ""), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
