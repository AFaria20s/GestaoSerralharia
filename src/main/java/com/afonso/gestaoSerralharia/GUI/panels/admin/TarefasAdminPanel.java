package com.afonso.gestaoSerralharia.GUI.panels.admin;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import java.awt.*;
public class TarefasAdminPanel extends BasePanel {
    public TarefasAdminPanel() {
        add(buildHeader("Tarefas", "RF11 — atribuir tarefas a funcionarios"), BorderLayout.NORTH);
        add(buildEmptyState("Em construção"), BorderLayout.CENTER);
    }
}
