package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.admin.*;
import com.afonso.gestaoSerralharia.GUI.panels.func.*;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.services.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class AppFrame extends JFrame {

    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_ADMIN = "admin";
    private static final String VIEW_FUNC  = "func";

    private final AuthService    authService;
    private final SessionManager session = SessionManager.getInstance();

    private CardLayout rootLayout;
    private JPanel     rootContainer;

    private CardLayout adminCardLayout;
    private JPanel     adminContent;
    private CardLayout funcCardLayout;
    private JPanel     funcContent;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String panelAtivo = "";

    private JTextField     loginEmailField;
    private JPasswordField loginPasswordField;
    private JLabel         loginErroLabel;

    public AppFrame(AuthService authService) {
        this.authService = authService;

        setTitle("Serralharia");
        setMinimumSize(new Dimension(UIConstants.APP_MIN_WIDTH, UIConstants.APP_MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildRootLayout();
        showView(VIEW_LOGIN);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ROOT
    // ─────────────────────────────────────────────────────────────────────────

    private void buildRootLayout() {
        rootLayout    = new CardLayout();
        rootContainer = new JPanel(rootLayout);

        rootContainer.add(buildLoginView(), VIEW_LOGIN);
        rootContainer.add(buildAppView(true),  VIEW_ADMIN);
        rootContainer.add(buildAppView(false), VIEW_FUNC);

        add(rootContainer);
    }

    private void showView(String view) {
        rootLayout.show(rootContainer, view);
        if (view.equals(VIEW_LOGIN)) {
            setExtendedState(JFrame.NORMAL);
            setSize(UIConstants.LOGIN_WIDTH, UIConstants.LOGIN_HEIGHT);
            setLocationRelativeTo(null);
            setResizable(false);
        } else {
            setResizable(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildLoginView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(bg());
        root.add(buildLoginAccent(), BorderLayout.WEST);
        root.add(buildLoginForm(),   BorderLayout.CENTER);
        return root;
    }

    private JPanel buildLoginAccent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(UIConstants.LOGIN_ACCENT_WIDTH, 0));
        panel.setBackground(UIConstants.COLOR_LOGIN_ACCENT_BG);

        JLabel icon  = label("⚙", 30f, UIConstants.COLOR_LOGIN_ACCENT_ICON, Font.PLAIN);
        JLabel line1 = label("Serra-", 15f, Color.WHITE, Font.BOLD);
        JLabel line2 = label("lharia", 15f, Color.WHITE, Font.BOLD);

        for (JLabel l : new JLabel[]{icon, line1, line2})
            l.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(line1);
        panel.add(line2);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bg());
        panel.setBorder(new EmptyBorder(
                UIConstants.LOGIN_FORM_PAD_TOP,
                UIConstants.LOGIN_FORM_PAD_H,
                UIConstants.LOGIN_FORM_PAD_H,
                UIConstants.LOGIN_FORM_PAD_H));

        JLabel titulo = label("Entrar", UIConstants.FONT_LOGIN_TITLE, null, Font.BOLD);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = label("Gestão de Obras", UIConstants.FONT_PANEL_SUB, muted(), Font.PLAIN);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(3, 0, 24, 0));

        loginEmailField    = new JTextField();
        loginPasswordField = new JPasswordField();
        loginErroLabel     = buildErroLabel();

        JButton btn = new JButton("Entrar");
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.LOGIN_BTN_HEIGHT));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> tentarLogin());

        loginEmailField.addActionListener(e -> loginPasswordField.requestFocusInWindow());
        loginPasswordField.addActionListener(e -> tentarLogin());

        panel.add(titulo);
        panel.add(sub);
        panel.add(fieldGroup("Email",    loginEmailField));
        panel.add(Box.createVerticalStrut(UIConstants.LOGIN_FIELD_GAP));
        panel.add(fieldGroup("Password", loginPasswordField));
        panel.add(Box.createVerticalStrut(UIConstants.LOGIN_FIELD_GAP - 2));
        panel.add(loginErroLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btn);
        return panel;
    }

    private void tentarLogin() {
        String email    = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        loginErroLabel.setText(" ");

        if (email.isEmpty() || password.isEmpty()) {
            loginErroLabel.setText("Preenche todos os campos.");
            return;
        }

        if (authService.login(email, password)) {
            loginPasswordField.setText("");
            loginEmailField.setText("");
            setTitle("Serralharia — " + session.getNome());
            showView(session.isDono() ? VIEW_ADMIN : VIEW_FUNC);
        } else {
            loginErroLabel.setText("Email ou password incorretos.");
            loginPasswordField.setText("");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  APP VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildAppView(boolean isAdmin) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(bg());

        CardLayout cl      = new CardLayout();
        JPanel     content = new JPanel(cl);
        content.setBackground(bg());

        if (isAdmin) {
            adminCardLayout = cl;
            adminContent    = content;
            registerAdminPanels();
        } else {
            funcCardLayout = cl;
            funcContent    = content;
            registerFuncPanels();
        }

        root.add(buildSidebar(isAdmin), BorderLayout.WEST);
        root.add(content,               BorderLayout.CENTER);
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SIDEBAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildSidebar(boolean isAdmin) {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sb.setMinimumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sb.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, Integer.MAX_VALUE));
        sb.setBackground(bg());
        sb.setBorder(new MatteBorder(0, 0, 0, 1, border()));

        Color accent = isAdmin ? UIConstants.COLOR_ADMIN_ACCENT : UIConstants.COLOR_FUNC_ACCENT;
        String role  = isAdmin ? "Administrador" : "Funcionário";

        sb.add(sidebarLogo(role, accent));

        if (isAdmin) {
            sb.add(sidebarSection("PRINCIPAL"));
            sb.add(sidebarBtn("dashboard",    "Dashboard",    isAdmin));
            sb.add(sidebarSection("OBRAS"));
            sb.add(sidebarBtn("obras",        "Obras",        isAdmin));
            sb.add(sidebarBtn("visitas",      "Visitas",      isAdmin));
            sb.add(sidebarBtn("orcamentos",   "Orçamentos",   isAdmin));
            sb.add(sidebarSection("EQUIPA"));
            sb.add(sidebarBtn("funcionarios", "Funcionários", isAdmin));
            sb.add(sidebarBtn("equipas",      "Equipas",      isAdmin));
            sb.add(sidebarBtn("tarefas",      "Tarefas",      isAdmin));
            sb.add(sidebarSection("COMERCIAL"));
            sb.add(sidebarBtn("clientes",     "Clientes",     isAdmin));
            sb.add(sidebarBtn("stock",        "Stock",        isAdmin));
            sb.add(sidebarBtn("faturacao",    "Faturação",    isAdmin));
        } else {
            sb.add(sidebarSection("INÍCIO"));
            sb.add(sidebarBtn("inicio",        "Início",            isAdmin));
            sb.add(sidebarSection("O MEU TRABALHO"));
            sb.add(sidebarBtn("minhastarefas", "Minhas Tarefas",    isAdmin));
            sb.add(sidebarBtn("minhaequipa",   "Minha Equipa",      isAdmin));
            sb.add(sidebarBtn("minhasobras",   "Minhas Obras",      isAdmin));
            sb.add(sidebarSection("REPORTAR"));
            sb.add(sidebarBtn("reportar",      "Reportar Problema", isAdmin));
            sb.add(sidebarBtn("processo",      "Registar Processo", isAdmin));
        }

        sb.add(Box.createVerticalGlue());
        sb.add(sidebarFooter());
        return sb;
    }

    private JPanel sidebarLogo(String role, Color accent) {
        JLabel nome  = label("Serralharia", UIConstants.FONT_SIDEBAR_LOGO, null, Font.BOLD);
        JLabel lRole = label(role, UIConstants.FONT_SIDEBAR_ROLE, accent, Font.PLAIN);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_LOGO_PAD_V, UIConstants.SIDEBAR_PAD_H,
                UIConstants.SIDEBAR_LOGO_PAD_V, UIConstants.SIDEBAR_PAD_H));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(nome);
        text.add(lRole);
        inner.add(text, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_LOGO_HEIGHT));
        wrapper.setBorder(new MatteBorder(0, 0, 1, 0, border()));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel sidebarSection(String texto) {
        JLabel lbl = label(texto, UIConstants.FONT_SIDEBAR_SEC, muted(), Font.BOLD);
        lbl.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_SECTION_TOP, UIConstants.SIDEBAR_PAD_H,
                UIConstants.SIDEBAR_SECTION_BOT, UIConstants.SIDEBAR_PAD_H));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        wrapper.add(lbl, BorderLayout.WEST);
        return wrapper;
    }

    private JPanel sidebarBtn(String id, String text, boolean isAdmin) {
        Color hoverBg  = isAdmin ? UIConstants.COLOR_ADMIN_HOVER_BG  : UIConstants.COLOR_FUNC_HOVER_BG;
        Color activeFg = isAdmin ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG;

        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(UIConstants.FONT_SIDEBAR_BTN));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H,
                UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!id.equals(panelAtivo)) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(hoverBg);
                    btn.setForeground(activeFg);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!id.equals(panelAtivo)) {
                    btn.setContentAreaFilled(false);
                    btn.setForeground(UIManager.getColor("Label.foreground"));
                }
            }
        });

        btn.addActionListener(e -> showPanel(id));
        navButtons.put(id, btn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_BTN_HEIGHT));
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel sidebarFooter() {
        JLabel nome = label(session.getNome(), UIConstants.FONT_SIDEBAR_FOOT, null, Font.BOLD);

        JButton btnLogout = new JButton("Terminar sessão");
        btnLogout.setFont(btnLogout.getFont().deriveFont(UIConstants.FONT_SMALL));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setForeground(UIConstants.COLOR_DANGER);
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnLogout.addActionListener(e -> logout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_FOOTER_PAD_V, UIConstants.SIDEBAR_PAD_H,
                UIConstants.SIDEBAR_FOOTER_PAD_V + 2, UIConstants.SIDEBAR_PAD_H));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(nome);
        text.add(btnLogout);
        inner.add(text, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_FOOTER_HEIGHT));
        wrapper.setBorder(new MatteBorder(1, 0, 0, 0, border()));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PANELS
    // ─────────────────────────────────────────────────────────────────────────

    private void registerAdminPanels() {
        addAdminPanel("dashboard",    new DashboardPanel());
        addAdminPanel("obras",        new ObrasPanel());
        addAdminPanel("visitas",      new VisitasPanel());
        addAdminPanel("orcamentos",   new OrcamentosPanel());
        addAdminPanel("funcionarios", new FuncionariosPanel());
        addAdminPanel("equipas",      new EquipasPanel());
        addAdminPanel("tarefas",      new TarefasAdminPanel());
        addAdminPanel("clientes",     new ClientesPanel());
        addAdminPanel("stock",        new StockPanel());
        addAdminPanel("faturacao",    new FaturacaoPanel());
    }

    private void registerFuncPanels() {
        addFuncPanel("inicio",        new InicioFuncPanel());
        addFuncPanel("minhastarefas", new MinhasTarefasPanel());
        addFuncPanel("minhaequipa",   new MinhaEquipaPanel());
        addFuncPanel("minhasobras",   new MinhasObrasPanel());
        addFuncPanel("reportar",      new ReportarProblemaPanel());
        addFuncPanel("processo",      new RegistarProcessoPanel());
    }

    private void addAdminPanel(String id, JPanel panel) { adminContent.add(panel, id); }
    private void addFuncPanel(String id, JPanel panel)  { funcContent.add(panel, id); }

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
                    ? UIConstants.COLOR_ADMIN_HOVER_BG : UIConstants.COLOR_FUNC_HOVER_BG);
            atual.setForeground(session.isDono()
                    ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG);
        }

        panelAtivo = id;
        CardLayout cl      = session.isDono() ? adminCardLayout : funcCardLayout;
        JPanel     content = session.isDono() ? adminContent    : funcContent;
        cl.show(content, id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    private void logout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que queres terminar a sessão?",
                "Terminar sessão", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        session.logout();
        navButtons.clear();
        panelAtivo = "";
        setTitle("Serralharia");
        showView(VIEW_LOGIN);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel fieldGroup(String labelText, JComponent field) {
        JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));
        g.setOpaque(false);
        g.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = label(labelText, UIConstants.FONT_FIELD_LABEL, null, Font.PLAIN);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        g.add(lbl);
        g.add(field);
        return g;
    }

    private JLabel buildErroLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_SMALL));
        lbl.setForeground(UIConstants.COLOR_DANGER);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel label(String text, float size, Color color, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(style, size));
        if (color != null) lbl.setForeground(color);
        return lbl;
    }

    private Color bg()     { Color c = UIManager.getColor("Panel.background");        return c != null ? c : Color.WHITE; }
    private Color muted()  { Color c = UIManager.getColor("Label.disabledForeground"); return c != null ? c : Color.GRAY; }
    private Color border() { Color c = UIManager.getColor("Component.borderColor");    return c != null ? c : new Color(226, 232, 240); }
}