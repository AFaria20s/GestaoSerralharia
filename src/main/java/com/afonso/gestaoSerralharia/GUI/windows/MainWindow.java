package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.GUI.panels.admin.*;
import com.afonso.gestaoSerralharia.GUI.panels.func.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends JFrame {

    private final SessionManager session = SessionManager.getInstance();

    private CardLayout cardLayout;
    private JPanel contentArea;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String panelAtivo = "";

    public MainWindow() {
        setTitle("Serralharia — " + session.getNome());
        setSize(1280, 760);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(960, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIManager.getColor("Panel.background"));

        JPanel sidebar = session.isDono() ? buildAdminSidebar() : buildFuncSidebar();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentArea);
        split.setDividerSize(0);
        split.setEnabled(false);
        add(split, BorderLayout.CENTER);

        if (session.isDono()) {
            addPanel("dashboard",    new DashboardPanel());
            addPanel("obras",        new ObrasPanel());
            addPanel("visitas",      new VisitasPanel());
            addPanel("orcamentos",   new OrcamentosPanel());
            addPanel("funcionarios", new FuncionariosPanel());
            addPanel("equipas",      new EquipasPanel());
            addPanel("tarefas",      new TarefasAdminPanel());
            addPanel("clientes",     new ClientesPanel());
            addPanel("stock",        new StockPanel());
            addPanel("faturacao",    new FaturacaoPanel());
            showPanel("dashboard");
        } else {
            addPanel("inicio",        new InicioFuncPanel());
            addPanel("minhastarefas", new MinhasTarefasPanel());
            addPanel("minhaequipa",   new MinhaEquipaPanel());
            addPanel("minhasobras",   new MinhasObrasPanel());
            addPanel("reportar",      new ReportarProblemaPanel());
            addPanel("processo",      new RegistarProcessoPanel());
            showPanel("inicio");
        }

        setVisible(true);
    }

    private JPanel buildAdminSidebar() {
        JPanel sb = newSidebar();
        sb.add(buildLogo(new Color(59, 130, 246)));
        sb.add(buildSection("PRINCIPAL"));
        sb.add(navBtn("dashboard",    "📊", "Dashboard",       "a"));
        sb.add(buildSection("OBRAS"));
        sb.add(navBtn("obras",        "🏗", "Obras",           "a"));
        sb.add(navBtn("visitas",      "📅", "Visitas",         "a"));
        sb.add(navBtn("orcamentos",   "📄", "Orçamentos",      "a"));
        sb.add(buildSection("EQUIPA"));
        sb.add(navBtn("funcionarios", "👷", "Funcionários",    "a"));
        sb.add(navBtn("equipas",      "👥", "Equipas",         "a"));
        sb.add(navBtn("tarefas",      "✅", "Tarefas",         "a"));
        sb.add(buildSection("COMERCIAL"));
        sb.add(navBtn("clientes",     "🧑", "Clientes",        "a"));
        sb.add(navBtn("stock",        "📦", "Stock",           "a"));
        sb.add(navBtn("faturacao",    "💶", "Faturação",       "a"));
        sb.add(Box.createVerticalGlue());
        sb.add(buildFooter());
        return sb;
    }

    private JPanel buildFuncSidebar() {
        JPanel sb = newSidebar();
        sb.add(buildLogo(new Color(22, 163, 74)));
        sb.add(buildSection("INÍCIO"));
        sb.add(navBtn("inicio",        "🏠", "Início",            "f"));
        sb.add(buildSection("O MEU TRABALHO"));
        sb.add(navBtn("minhastarefas", "✅", "Minhas Tarefas",    "f"));
        sb.add(navBtn("minhaequipa",   "👥", "Minha Equipa",      "f"));
        sb.add(navBtn("minhasobras",   "🏗", "Minhas Obras",      "f"));
        sb.add(buildSection("REPORTAR"));
        sb.add(navBtn("reportar",      "⚠", "Reportar Problema", "f"));
        sb.add(navBtn("processo",      "📝", "Registar Processo", "f"));
        sb.add(Box.createVerticalGlue());
        sb.add(buildFooter());
        return sb;
    }

    public void showPanel(String id) {
        JButton anterior = navButtons.get(panelAtivo);
        if (anterior != null) {
            anterior.setContentAreaFilled(false);
            anterior.setForeground(UIManager.getColor("Label.foreground"));
        }

        JButton atual = navButtons.get(id);
        if (atual != null) {
            atual.setContentAreaFilled(true);
            atual.setBackground(session.isDono()
                    ? new Color(239, 246, 255)
                    : new Color(240, 253, 244));
            atual.setForeground(session.isDono()
                    ? new Color(29, 78, 216)
                    : new Color(21, 128, 61));
        }

        panelAtivo = id;
        cardLayout.show(contentArea, id);
    }

    private void addPanel(String id, JPanel panel) {
        contentArea.add(panel, id);
    }

    private JPanel newSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(200, 0));
        sb.setMinimumSize(new Dimension(200, 0));
        sb.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, borderColor()));
        sb.setBackground(UIManager.getColor("Panel.background"));
        return sb;
    }

    private JPanel buildLogo(Color accentColor) {
        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 2));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel nome = new JLabel("Serralharia");
        nome.setFont(nome.getFont().deriveFont(Font.BOLD, 14f));

        JLabel role = new JLabel(session.getRole());
        role.setFont(role.getFont().deriveFont(11f));
        role.setForeground(accentColor);

        inner.add(nome);
        inner.add(role);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new MatteBorder(0, 0, 1, 0, borderColor()));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        wrapper.add(inner);
        return wrapper;
    }

    private JLabel buildSection(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 10f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setBorder(new EmptyBorder(14, 16, 3, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton navBtn(String panelId, String icon, String label, String role) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(btn.getFont().deriveFont(13f));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        Color hoverBg = role.equals("a") ? new Color(239, 246, 255) : new Color(240, 253, 244);
        Color hoverFg = role.equals("a") ? new Color(29, 78, 216)   : new Color(21, 128, 61);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!panelId.equals(panelAtivo)) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(hoverBg);
                    btn.setForeground(hoverFg);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!panelId.equals(panelAtivo)) {
                    btn.setContentAreaFilled(false);
                    btn.setForeground(UIManager.getColor("Label.foreground"));
                }
            }
        });

        btn.addActionListener(e -> showPanel(panelId));
        navButtons.put(panelId, btn);
        return btn;
    }

    private JPanel buildFooter() {
        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 2));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(10, 16, 12, 16));

        JLabel nome = new JLabel(session.getNome());
        nome.setFont(nome.getFont().deriveFont(Font.BOLD, 12f));

        JButton btnLogout = new JButton("Terminar sessão");
        btnLogout.setFont(btnLogout.getFont().deriveFont(11f));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnLogout.addActionListener(e -> logout());

        inner.add(nome);
        inner.add(btnLogout);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new MatteBorder(1, 0, 0, 0, borderColor()));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        wrapper.add(inner);
        return wrapper;
    }

    private void logout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que queres terminar a sessão?",
                "Terminar sessão", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            System.exit(0);
        }
    }

    private Color borderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : new Color(226, 232, 240);
    }
}
