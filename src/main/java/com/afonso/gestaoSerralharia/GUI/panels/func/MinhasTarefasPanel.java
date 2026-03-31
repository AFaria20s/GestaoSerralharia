package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class MinhasTarefasPanel extends BasePanel {
    public MinhasTarefasPanel() {
        add(buildHeader("Minhas Tarefas", ""), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
