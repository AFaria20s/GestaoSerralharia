package com.afonso.gestaoSerralharia.GUI.panels.func;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class MinhasTarefasPanel extends BasePanel {
    public MinhasTarefasPanel() {
        add(buildHeader("Minhas Tarefas", "RF16 — filtrar por prazo"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
