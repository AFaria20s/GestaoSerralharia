package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class InicioFuncPanel extends BasePanel {
    public InicioFuncPanel() {
        add(buildHeader("Inicio", "Resumo do teu dia"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
