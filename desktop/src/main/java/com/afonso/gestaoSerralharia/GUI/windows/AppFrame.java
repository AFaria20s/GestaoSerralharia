package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.GUI.panels.admin.*;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class AppFrame extends JFrame {

    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_ADMIN = "admin";

    private final AuthService    authService;
    private final SessionManager session = SessionManager.getInstance();

    private CardLayout rootLayout;
    private JPanel     rootContainer;

    private CardLayout adminCardLayout;
    private FadePanel  adminContent;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private final List<JLabel> sidebarAvatarLabels = new ArrayList<>();
    private final List<JLabel> sidebarRoleLabels = new ArrayList<>();
    private final List<AbstractButton> sidebarProfileButtons = new ArrayList<>();
    private final Map<String, JPanel> adminPanels = new HashMap<>();
    private String panelAtivo = "";
    private Timer fadeTimer;
    private final Preferences preferences = Preferences.userNodeForPackage(AppFrame.class);
    private boolean animationsEnabled = true;

    private JTextField     loginEmailField;
    private JPasswordField loginPasswordField;
    private JLabel         loginErroLabel;

    private FuncionarioService funcionarioService;
    private CargoService cargoService;
    private final DonoService donoService;

    private final ObraService        obraService;
    private final TarefaService      tarefaService;
    private final FaturaService      faturaService;
    private final ProblemaService problemaService;
    private final ClienteService clienteService;
    private final EstadoobraService estadoObraService;
    private final CodpostalService codPostalService;
    private final VisitaService visitaService;
    private final OrcamentoService orcamentoService;
    private final LinhaorcamentoService    linhaorcamentoService;
    private final TaxaivaService           taxaivaService;
    private final TipolinhaorcamentoService tipolinhaorcamentoService;
    private final GravidadeproblemaService gravidadeService;
    private final EquipafuncionarioService equipafuncionarioService;
    private final EquipaService equipaService;
    private final EstadopagamentoService estadoPagamentoService;
    private final MaterialService materialService;
    private final FornecedorService fornecedorService;
    private final EncomendaService encomendaService;
    private final LinhaencomendaService linhaEncomendaService;
    private final EstadotarefaService estadotarefaService;
    private final MovimentofinanceiroService movimentofinanceiroService;

    public AppFrame(AuthService authService,
                    ObraService obraService,
                    TarefaService tarefaService,
                    FaturaService faturaService,
                    ProblemaService problemaService,
                    ClienteService clienteService,
                    DonoService donoService,
                    FuncionarioService funcionarioService,
                    CargoService cargoService,
                    EstadoobraService estadoObraService,
                    CodpostalService codPostalService,
                    VisitaService visitaService,
                    OrcamentoService orcamentoService,
                    LinhaorcamentoService linhaorcamentoService,
                    TaxaivaService taxaivaService,
                    TipolinhaorcamentoService tipolinhaorcamentoService,
                    GravidadeproblemaService gravidadeService,
                    EquipafuncionarioService equipafuncionarioService,
                    EquipaService equipaService,
                    EstadopagamentoService estadoPagamentoService,
                    MaterialService materialService,
                    FornecedorService fornecedorService,
                    EncomendaService encomendaService,
                    LinhaencomendaService linhaEncomendaService,
                    EstadotarefaService estadotarefaService,
                    MovimentofinanceiroService movimentofinanceiroService
    ) {
        this.authService                = authService;
        this.obraService                = obraService;
        this.tarefaService              = tarefaService;
        this.faturaService              = faturaService;
        this.problemaService            = problemaService;
        this.clienteService             = clienteService;
        this.donoService                = donoService;
        this.funcionarioService         = funcionarioService;
        this.cargoService               = cargoService;
        this.estadoObraService          = estadoObraService;
        this.codPostalService           = codPostalService;
        this.visitaService              = visitaService;
        this.orcamentoService           = orcamentoService;
        this.linhaorcamentoService      = linhaorcamentoService;
        this.taxaivaService             = taxaivaService;
        this.tipolinhaorcamentoService  = tipolinhaorcamentoService;
        this.gravidadeService           = gravidadeService;
        this.equipafuncionarioService   = equipafuncionarioService;
        this.equipaService              = equipaService;
        this.estadoPagamentoService     = estadoPagamentoService;
        this.materialService            = materialService;
        this.fornecedorService          = fornecedorService;
        this.encomendaService           = encomendaService;
        this.linhaEncomendaService      = linhaEncomendaService;
        this.estadotarefaService        = estadotarefaService;
        this.movimentofinanceiroService = movimentofinanceiroService;
        this.animationsEnabled = preferences.getBoolean("animations_enabled", true);

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
        rootContainer.add(buildAppView(), VIEW_ADMIN);

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
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(244, 247, 252));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Color.WHITE);
        shell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 238)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        shell.setPreferredSize(new Dimension(UIConstants.LOGIN_WIDTH, UIConstants.LOGIN_HEIGHT));
        shell.add(buildLoginAccent(), BorderLayout.WEST);
        shell.add(buildLoginForm(), BorderLayout.CENTER);

        root.add(shell);
        return root;
    }

    private JPanel buildLoginAccent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(140, 0));
        panel.setBackground(new Color(32, 52, 82));

        JLabel line1 = label("Serralharia", 16f, Color.WHITE, Font.BOLD);
        JLabel line2 = label("Backoffice", 13f, new Color(191, 219, 254), Font.PLAIN);

        line1.setAlignmentX(CENTER_ALIGNMENT);
        line2.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(line1);
        panel.add(Box.createVerticalStrut(6));
        panel.add(line2);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(
                UIConstants.LOGIN_FORM_PAD_TOP,
                UIConstants.LOGIN_FORM_PAD_H,
                UIConstants.LOGIN_FORM_PAD_H,
                UIConstants.LOGIN_FORM_PAD_H));

        JLabel titulo = label("Iniciar sessão", 24f, new Color(15, 23, 42), Font.BOLD);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = label("Acede à área de gestão", UIConstants.FONT_PANEL_SUB, new Color(100, 116, 139), Font.PLAIN);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(3, 0, 24, 0));

        loginEmailField    = new JTextField();
        loginPasswordField = new JPasswordField();
        loginErroLabel     = buildErroLabel();

        JButton btn = new JButton("Entrar");
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setFocusPainted(false);
        btn.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btn.setForeground(Color.WHITE);
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
        panel.add(Box.createVerticalStrut(16));
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
            setTitle("Serralharia - " + session.getNome());
            refreshSidebarProfile();
            showView(VIEW_ADMIN);
            showPanel(session.getPainelInicial());
        } else {
            loginErroLabel.setText("Email ou password incorretos.");
            loginPasswordField.setText("");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  APP VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildAppView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(bg());

        CardLayout cl      = new CardLayout();
        FadePanel content = new FadePanel(cl);
        content.setBackground(bg());

        adminCardLayout = cl;
        adminContent    = content;
        registerAdminPanels();

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content,               BorderLayout.CENTER);
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SIDEBAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sb.setMinimumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sb.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, Integer.MAX_VALUE));
        sb.setBackground(new Color(242, 246, 252));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, new Color(216, 223, 235)));

        Color accent = UIConstants.COLOR_ADMIN_ACCENT;
        String role  = "Administrador";

        sb.add(sidebarLogo(role, accent));

        sb.add(sidebarSection("PRINCIPAL"));
        sb.add(sidebarBtn("dashboard",    "Dashboard"));
        sb.add(sidebarSection("OBRAS"));
        sb.add(sidebarBtn("obras",        "Obras"));
        sb.add(sidebarBtn("visitas",      "Visitas"));
        sb.add(sidebarBtn("problemas",    "Problemas"));
        sb.add(sidebarBtn("orcamentos",   "Orçamentos"));
        sb.add(sidebarSection("EQUIPA"));
        sb.add(sidebarBtn("funcionarios", "Funcionários"));
        sb.add(sidebarBtn("equipas",      "Equipas"));
        sb.add(sidebarBtn("tarefas",      "Tarefas"));
        sb.add(sidebarSection("COMERCIAL"));
        sb.add(sidebarBtn("clientes",     "Clientes"));
        sb.add(sidebarBtn("stock",        "Stock"));
        sb.add(sidebarBtn("faturacao",    "Faturação"));
        sb.add(sidebarBtn("movimentos",   "Movimentos"));

        sb.add(Box.createVerticalGlue());
        sb.add(sidebarFooter());
        return sb;
    }

    private JPanel sidebarLogo(String role, Color accent) {
        JLabel nome  = label("Serralharia", UIConstants.FONT_SIDEBAR_LOGO, null, Font.BOLD);
        JLabel lRole = label(role, UIConstants.FONT_SIDEBAR_ROLE, accent, Font.PLAIN);
        JLabel avatar = buildSidebarAvatar(accent);
        sidebarAvatarLabels.add(avatar);
        sidebarRoleLabels.add(lRole);

        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_LOGO_PAD_V, UIConstants.SIDEBAR_PAD_H + 4,
                UIConstants.SIDEBAR_LOGO_PAD_V, UIConstants.SIDEBAR_PAD_H + 4));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(nome);
        text.add(lRole);
        inner.add(avatar, BorderLayout.WEST);
        inner.add(text, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_LOGO_HEIGHT + 8));
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

    private JPanel sidebarBtn(String id, String text) {
        Color activeFg = UIConstants.COLOR_ADMIN_ACTIVE_FG;
        Color outline  = UIConstants.COLOR_ADMIN_ACCENT;

        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(UIConstants.FONT_SIDEBAR_BTN));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(new EmptyBorder(
                UIConstants.SIDEBAR_PAD_V + 1, UIConstants.SIDEBAR_PAD_H + 10,
                UIConstants.SIDEBAR_PAD_V + 1, UIConstants.SIDEBAR_PAD_H + 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!id.equals(panelAtivo)) {
                    applySidebarButtonStyle(btn, true, false, outline, activeFg);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!id.equals(panelAtivo)) {
                    applySidebarButtonStyle(btn, false, false, outline, activeFg);
                }
            }
        });

        btn.addActionListener(e -> showPanel(id));
        navButtons.put(id, btn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_BTN_HEIGHT));
        wrapper.setBorder(new EmptyBorder(0, 6, 0, 6));
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel sidebarFooter() {
        JButton btnPerfil = new JButton(session.getNome());
        btnPerfil.setFont(btnPerfil.getFont().deriveFont(UIConstants.FONT_SIDEBAR_FOOT));
        btnPerfil.setBorderPainted(false);
        btnPerfil.setContentAreaFilled(false);
        btnPerfil.setFocusPainted(false);
        btnPerfil.setForeground(UIManager.getColor("Label.foreground"));
        btnPerfil.setHorizontalAlignment(SwingConstants.LEFT);
        btnPerfil.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPerfil.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnPerfil.addActionListener(e -> abrirPerfilDialogo());
        sidebarProfileButtons.add(btnPerfil);

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
                UIConstants.SIDEBAR_FOOTER_PAD_V, UIConstants.SIDEBAR_PAD_H + 4,
                UIConstants.SIDEBAR_FOOTER_PAD_V + 2, UIConstants.SIDEBAR_PAD_H + 4));

        JLabel editar = label("Editar perfil", UIConstants.FONT_SMALL, muted(), Font.PLAIN);

        JPanel text = new JPanel(new GridLayout(3, 1, 0, 2));
        text.setOpaque(false);
        text.add(btnPerfil);
        text.add(editar);
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
        addAdminPanel("dashboard",    new DashboardPanel(
                obraService, tarefaService, faturaService,
                problemaService, clienteService, funcionarioService,
                orcamentoService, visitaService, movimentofinanceiroService));
        addAdminPanel("obras",        new ObrasPanel(
                obraService, clienteService, estadoObraService,
                visitaService, orcamentoService, codPostalService,
                problemaService, gravidadeService));
        addAdminPanel("visitas",      new VisitasPanel(visitaService, obraService));
        addAdminPanel("problemas",    new ProblemasPanel(
                problemaService, obraService, gravidadeService, tarefaService));
        addAdminPanel("orcamentos", new OrcamentosPanel(
                orcamentoService,
                linhaorcamentoService,
                obraService,
                materialService,
                taxaivaService,
                tipolinhaorcamentoService));
        addAdminPanel("funcionarios", new FuncionariosPanel(funcionarioService, cargoService));
        addAdminPanel("equipas",      new EquipasPanel(
                equipaService, equipafuncionarioService,
                obraService, funcionarioService, tarefaService));
        addAdminPanel("tarefas",      new TarefasAdminPanel(
                tarefaService, obraService, funcionarioService, estadotarefaService,
                equipaService, equipafuncionarioService));
        addAdminPanel("clientes",     new ClientesPanel(clienteService, codPostalService));
        addAdminPanel("stock",        new StockPanel(materialService, fornecedorService, encomendaService, linhaEncomendaService));
        addAdminPanel("faturacao",    new FaturacaoPanel(faturaService, obraService, orcamentoService, linhaorcamentoService, estadoPagamentoService));
        addAdminPanel("movimentos",   new MovimentosFinanceirosPanel(movimentofinanceiroService));
    }

    private void addAdminPanel(String id, JPanel panel) {
        adminPanels.put(id, panel);
        adminContent.add(panel, id);
    }

    public void showPanel(String id) {
        JButton anterior = navButtons.get(panelAtivo);
        if (anterior != null) {
            applySidebarButtonStyle(anterior, false, false,
                    UIConstants.COLOR_ADMIN_ACCENT,
                    UIConstants.COLOR_ADMIN_ACTIVE_FG);
        }

        JButton atual = navButtons.get(id);
        if (atual != null) {
            applySidebarButtonStyle(atual, false, true,
                    UIConstants.COLOR_ADMIN_ACCENT,
                    UIConstants.COLOR_ADMIN_ACTIVE_FG);
        }

        panelAtivo = id;
        adminCardLayout.show(adminContent, id);
        animatePanelFade();
        JPanel panel = adminPanels.get(id);
        if (panel instanceof BasePanel basePanel) {
            basePanel.onPanelShown();
        }
    }

    private void animatePanelFade() {
        if (!animationsEnabled) {
            adminContent.setAlpha(1f);
            return;
        }
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        adminContent.setAlpha(0.0f);
        final int durationMs = 240;
        final int interval = 15;
        final int steps = Math.max(1, durationMs / interval);
        fadeTimer = new Timer(interval, null);
        fadeTimer.addActionListener(e -> {
            float alpha = adminContent.getAlpha() + (1.0f / steps);
            if (alpha >= 1f) {
                adminContent.setAlpha(1f);
                fadeTimer.stop();
            } else {
                adminContent.setAlpha(alpha);
            }
        });
        fadeTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    private void logout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que queres terminar a sessão?",
                "Terminar sessão", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        resetSidebarButtons();
        session.logout();
        panelAtivo = "";
        setTitle("Serralharia");
        refreshSidebarProfile();
        showView(VIEW_LOGIN);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel buildSidebarAvatar(Color accent) {
        JLabel avatar = new JLabel();
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(true);
        avatar.setBackground(accent);
        avatar.setForeground(Color.WHITE);
        avatar.setBorder(new LineBorder(accent.darker(), 1, true));
        avatar.setText("SF");
        return avatar;
    }

    private void applySidebarButtonStyle(JButton btn, boolean hovered, boolean active, Color outline, Color accentText) {
        btn.setForeground(active || hovered ? accentText : new Color(51, 65, 85));
        btn.setContentAreaFilled(active || hovered);
        btn.setBackground(active ? new Color(214, 230, 255) : new Color(235, 243, 253));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder((active || hovered) ? outline : new Color(0, 0, 0, 0), 1, true),
                new EmptyBorder(UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H + 10,
                        UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H + 10)));
    }

    private void resetSidebarButtons() {
        for (JButton btn : navButtons.values()) {
            applySidebarButtonStyle(btn, false, false,
                    UIConstants.COLOR_ADMIN_ACCENT,
                    UIConstants.COLOR_ADMIN_ACTIVE_FG);
        }
    }

    private void refreshSidebarProfile() {
        String role = session.getRole();
        String nome = session.getNome();
        String path = session.getImagemPerfil();
        Color accent = new Color(255,255,255);

        for (JLabel roleLabel : sidebarRoleLabels) roleLabel.setText(role);
        for (AbstractButton button : sidebarProfileButtons) button.setText(nome);
        for (JLabel avatar : sidebarAvatarLabels) updateAvatarLabel(avatar, path, nome, accent);
    }

    private void updateAvatarLabel(JLabel avatar, String path, String nome, Color accent) {
        avatar.setIcon(null);
        avatar.setText(iniciais(nome));
        avatar.setBackground(accent);
        avatar.setBorder(new LineBorder(accent.darker(), 1, true));

        if (path == null || path.isBlank()) return;
        File file = new File(path);
        if (!file.isFile()) return;
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        avatar.setIcon(new ImageIcon(scaled));
        avatar.setText("");
    }

    private String iniciais(String nome) {
        if (nome == null || nome.isBlank()) return "SF";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }

    private void abrirPerfilDialogo() {
        if (!session.isAutenticado()) return;

        JDialog dlg = new JDialog(this, "Perfil", true);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.add(buildPerfilDono(dlg));
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildPerfilDono(JDialog dlg) {
        Dono atual = donoService.buscarPorId(session.getDono().getId());

        JTextField nome = new JTextField(atual.getNome());
        JTextField email = new JTextField(atual.getEmail());
        JPasswordField password = new JPasswordField();
        JTextField imagem = new JTextField(atual.getImagemPerfil() != null ? atual.getImagemPerfil() : "");
        JLabel preview = buildSidebarAvatar(UIConstants.COLOR_ADMIN_ACCENT);
        updateAvatarLabel(preview, imagem.getText().trim(), atual.getNome(), UIConstants.COLOR_ADMIN_ACCENT);
        JLabel erro = buildErroLabel();

        JComboBox<String> painelInicial = new JComboBox<>(new String[]{"dashboard", "obras", "orcamentos", "faturacao", "tarefas", "stock"});
        painelInicial.setSelectedItem(atual.getPainelInicial() != null ? atual.getPainelInicial() : "dashboard");
        JCheckBox chkAnimacoes = new JCheckBox("Ativar animações suaves");
        chkAnimacoes.setSelected(animationsEnabled);
        chkAnimacoes.setOpaque(false);

        JButton escolher = buildButton("Escolher imagem");
        escolher.addActionListener(e -> escolherImagem(imagem, preview, nome.getText(), UIConstants.COLOR_ADMIN_ACCENT));

        // ── Campos da empresa ──────────────────────────────────────────────
        JTextField empNome     = new JTextField(nvl(atual.getEmpresaNome()));
        JTextField empMorada   = new JTextField(nvl(atual.getEmpresaMorada()));
        JTextField empNif      = new JTextField(nvl(atual.getEmpresaNif()));
        JTextField empTelefone = new JTextField(nvl(atual.getEmpresaTelefone()));
        JTextField empEmail    = new JTextField(nvl(atual.getEmpresaEmail()));
        JTextField empIban     = new JTextField(nvl(atual.getEmpresaIban()));

        JButton cancelar = buildButton("Cancelar");
        cancelar.addActionListener(e -> dlg.dispose());
        JButton guardar = buildButton("Guardar");
        guardar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        guardar.setForeground(Color.WHITE);
        guardar.setOpaque(true);
        guardar.setBorderPainted(false);
        guardar.addActionListener(e -> {
            try {
                atual.setNome(nome.getText().trim());
                atual.setEmail(email.getText().trim());
                atual.setImagemPerfil(imagem.getText().trim().isBlank() ? null : imagem.getText().trim());
                atual.setPainelInicial((String) painelInicial.getSelectedItem());
                String novaPassword = new String(password.getPassword());
                if (!novaPassword.isBlank()) atual.setPassword(novaPassword);
                // dados empresa
                atual.setEmpresaNome(blank2null(empNome.getText()));
                atual.setEmpresaMorada(blank2null(empMorada.getText()));
                atual.setEmpresaNif(blank2null(empNif.getText()));
                atual.setEmpresaTelefone(blank2null(empTelefone.getText()));
                atual.setEmpresaEmail(blank2null(empEmail.getText()));
                atual.setEmpresaIban(blank2null(empIban.getText()));
                Dono guardado = donoService.guardar(atual);
                session.loginDono(guardado);
                animationsEnabled = chkAnimacoes.isSelected();
                preferences.putBoolean("animations_enabled", animationsEnabled);
                setTitle("Serralharia - " + session.getNome());
                refreshSidebarProfile();
                dlg.dispose();
            } catch (Exception ex) {
                erro.setText(ex.getMessage());
            }
        });

        JLabel extraInfo = new JLabel("Preferência da app: painel mostrado ao entrar.");
        extraInfo.setFont(extraInfo.getFont().deriveFont(UIConstants.FONT_SMALL));
        extraInfo.setForeground(muted());

        return buildPerfilForm("Perfil do administrador", preview, nome, email, password, imagem, escolher, erro,
                cancelar, guardar, painelInicial, extraInfo,
                empNome, empMorada, empNif, empTelefone, empEmail, empIban, chkAnimacoes);
    }

    private static String nvl(String s)       { return s != null ? s : ""; }
    private static String blank2null(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private JPanel buildPerfilForm(String titulo, JLabel preview, JTextField nome, JTextField email,
                                   JPasswordField password, JTextField imagem, JButton escolher,
                                   JLabel erro, JButton cancelar, JButton guardar,
                                   JComboBox<String> painelInicial, JLabel extraInfo,
                                   JTextField empNome, JTextField empMorada, JTextField empNif,
                                   JTextField empTelefone, JTextField empEmail, JTextField empIban,
                                   JCheckBox chkAnimacoes) {

        // Cabeçalho
        JLabel lblTitulo = label(titulo, 18f, null, Font.BOLD);
        JPanel topo = new JPanel(new BorderLayout(16, 0));
        topo.setOpaque(false);
        topo.add(preview, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);

        // Secção pessoal
        JPanel formPessoal = new JPanel(new GridBagLayout());
        formPessoal.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        int y = 0;
        c.gridy = y++; formPessoal.add(fieldGroup("Nome", nome), c);
        c.gridy = y++; formPessoal.add(fieldGroup("Email", email), c);
        c.gridy = y++; formPessoal.add(fieldGroup("Nova password (deixa em branco para não alterar)", password), c);
        c.gridy = y++; formPessoal.add(fieldGroup("Imagem de perfil", imagem), c);
        JPanel linhaImagem = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaImagem.setOpaque(false); linhaImagem.add(escolher);
        c.gridy = y++; formPessoal.add(linhaImagem, c);
        if (painelInicial != null) {
            c.gridy = y++; formPessoal.add(fieldGroup("Painel inicial", painelInicial), c);
            if (extraInfo != null) { c.gridy = y++; formPessoal.add(extraInfo, c); }
        }
        if (chkAnimacoes != null) {
            c.gridy = y++;
            JPanel animWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            animWrap.setOpaque(false);
            animWrap.add(chkAnimacoes);
            formPessoal.add(animWrap, c);
        }
        c.gridy = y; formPessoal.add(erro, c);


        // Separador empresa
        JLabel lblEmp = label("Dados da Empresa", 13f, null, Font.BOLD);
        lblEmp.setBorder(new EmptyBorder(18, 0, 4, 0));
        lblEmp.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblEmpSub = label("Estes dados aparecem no cabeçalho dos PDFs de orçamentos.", UIConstants.FONT_SMALL, muted(), Font.PLAIN);
        lblEmpSub.setBorder(new EmptyBorder(0, 0, 10, 0));
        lblEmpSub.setAlignmentX(LEFT_ALIGNMENT);

        // Secção empresa
        JPanel formEmp = new JPanel(new GridBagLayout());
        formEmp.setOpaque(false);
        GridBagConstraints ce = new GridBagConstraints();
        ce.gridx = 0; ce.weightx = 1; ce.fill = GridBagConstraints.HORIZONTAL;
        ce.insets = new Insets(0, 0, 10, 0);
        int ye = 0;
        ce.gridy = ye++; formEmp.add(fieldGroup("Nome da empresa", empNome), ce);
        ce.gridy = ye++; formEmp.add(fieldGroup("Morada", empMorada), ce);
        ce.gridy = ye++; formEmp.add(fieldGroup("NIF", empNif), ce);
        ce.gridy = ye++; formEmp.add(fieldGroup("Telefone", empTelefone), ce);
        ce.gridy = ye++; formEmp.add(fieldGroup("Email da empresa", empEmail), ce);
        ce.gridy = ye;   formEmp.add(fieldGroup("IBAN", empIban), ce);

        // Conteúdo com scroll
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.add(formPessoal);
        scrollContent.add(lblEmp);
        scrollContent.add(lblEmpSub);
        scrollContent.add(formEmp);
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        // Botões
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(cancelar); acoes.add(guardar);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 22, 18, 22));
        root.setPreferredSize(new Dimension(520, 580));
        root.add(topo,       BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(acoes,      BorderLayout.SOUTH);
        return root;
    }

    private void escolherImagem(JTextField campo, JLabel preview, String nome, Color accent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imagens", "png", "jpg", "jpeg", "gif", "webp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campo.setText(chooser.getSelectedFile().getAbsolutePath());
            updateAvatarLabel(preview, campo.getText().trim(), nome, accent);
        }
    }

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

    private JButton buildButton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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

    private static final class FadePanel extends JPanel {
        private float alpha = 1f;

        private FadePanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
        }

        float getAlpha() {
            return alpha;
        }

        void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int slideX = Math.round((1f - alpha) * 14f);
            g2.translate(slideX, 0);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        }
    }
}
