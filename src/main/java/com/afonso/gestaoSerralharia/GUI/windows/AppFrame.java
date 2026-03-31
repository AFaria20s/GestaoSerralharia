package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.admin.*;
import com.afonso.gestaoSerralharia.GUI.panels.func.*;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.models.Funcionario;
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
    private final List<JLabel> sidebarAvatarLabels = new ArrayList<>();
    private final List<JLabel> sidebarRoleLabels = new ArrayList<>();
    private final List<AbstractButton> sidebarProfileButtons = new ArrayList<>();
    private String panelAtivo = "";

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
                    EstadotarefaService estadotarefaService
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

        JLabel line1 = label("Serralharia", 15f, Color.WHITE, Font.BOLD);

        for (JLabel l : new JLabel[]{line1})
            l.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(line1);
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
            refreshSidebarProfile();
            showView(session.isDono() ? VIEW_ADMIN : VIEW_FUNC);
            showPanel(session.getPainelInicial());
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
        sb.setBackground(new Color(248, 250, 252));
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
            sb.add(sidebarBtn("problemas",    "Problemas",    isAdmin));
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
        JLabel avatar = buildSidebarAvatar(accent);
        sidebarAvatarLabels.add(avatar);
        sidebarRoleLabels.add(lRole);

        JPanel inner = new JPanel(new BorderLayout());
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

    private JPanel sidebarBtn(String id, String text, boolean isAdmin) {
        Color activeFg = isAdmin ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG;
        Color outline  = isAdmin ? UIConstants.COLOR_ADMIN_ACCENT : UIConstants.COLOR_FUNC_ACCENT;

        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(UIConstants.FONT_SIDEBAR_BTN));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
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
                orcamentoService, visitaService));
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
            applySidebarButtonStyle(anterior, false, false,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACCENT : UIConstants.COLOR_FUNC_ACCENT,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG);
        }

        JButton atual = navButtons.get(id);
        if (atual != null) {
            applySidebarButtonStyle(atual, false, true,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACCENT : UIConstants.COLOR_FUNC_ACCENT,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG);
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
        btn.setForeground(active || hovered ? accentText : UIManager.getColor("Label.foreground"));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder((active || hovered) ? outline : new Color(0, 0, 0, 0), 1, true),
                new EmptyBorder(UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H + 10,
                        UIConstants.SIDEBAR_PAD_V, UIConstants.SIDEBAR_PAD_H + 10)));
    }

    private void resetSidebarButtons() {
        for (JButton btn : navButtons.values()) {
            applySidebarButtonStyle(btn, false, false,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACCENT : UIConstants.COLOR_FUNC_ACCENT,
                    session.isDono() ? UIConstants.COLOR_ADMIN_ACTIVE_FG : UIConstants.COLOR_FUNC_ACTIVE_FG);
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
        dlg.add(session.isDono() ? buildPerfilDono(dlg) : buildPerfilFuncionario(dlg));
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildPerfilFuncionario(JDialog dlg) {
        Funcionario atual = funcionarioService.buscarPorId(session.getFuncionario().getId());

        JTextField nome = new JTextField(atual.getNome());
        JTextField email = new JTextField(atual.getEmail());
        JPasswordField password = new JPasswordField();
        JTextField imagem = new JTextField(atual.getImagemPerfil() != null ? atual.getImagemPerfil() : "");
        JLabel preview = buildSidebarAvatar(UIConstants.COLOR_FUNC_ACCENT);
        updateAvatarLabel(preview, imagem.getText().trim(), atual.getNome(), UIConstants.COLOR_FUNC_ACCENT);
        JLabel erro = buildErroLabel();

        JButton escolher = buildButton("Escolher imagem");
        escolher.addActionListener(e -> escolherImagem(imagem, preview, nome.getText(), UIConstants.COLOR_FUNC_ACCENT));

        JButton cancelar = buildButton("Cancelar");
        cancelar.addActionListener(e -> dlg.dispose());
        JButton guardar = buildButton("Guardar");
        guardar.setBackground(UIConstants.COLOR_FUNC_ACCENT);
        guardar.setForeground(Color.WHITE);
        guardar.setOpaque(true);
        guardar.setBorderPainted(false);
        guardar.addActionListener(e -> {
            try {
                atual.setNome(nome.getText().trim());
                atual.setEmail(email.getText().trim());
                atual.setImagemPerfil(imagem.getText().trim().isBlank() ? null : imagem.getText().trim());
                String novaPassword = new String(password.getPassword());
                if (!novaPassword.isBlank()) atual.setPassword(novaPassword);
                Funcionario guardado = funcionarioService.guardar(atual);
                session.loginFuncionario(guardado);
                setTitle("Serralharia — " + session.getNome());
                refreshSidebarProfile();
                dlg.dispose();
            } catch (Exception ex) {
                erro.setText(ex.getMessage());
            }
        });

        return buildPerfilForm("Perfil do utilizador", preview, nome, email, password, imagem, escolher, erro, cancelar, guardar, null, null);
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

        JButton escolher = buildButton("Escolher imagem");
        escolher.addActionListener(e -> escolherImagem(imagem, preview, nome.getText(), UIConstants.COLOR_ADMIN_ACCENT));

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
                Dono guardado = donoService.guardar(atual);
                session.loginDono(guardado);
                setTitle("Serralharia — " + session.getNome());
                refreshSidebarProfile();
                dlg.dispose();
            } catch (Exception ex) {
                erro.setText(ex.getMessage());
            }
        });

        JLabel extraInfo = new JLabel("Preferência da app: painel mostrado ao entrar.");
        extraInfo.setFont(extraInfo.getFont().deriveFont(UIConstants.FONT_SMALL));
        extraInfo.setForeground(muted());

        return buildPerfilForm("Perfil do administrador", preview, nome, email, password, imagem, escolher, erro, cancelar, guardar, painelInicial, extraInfo);
    }

    private JPanel buildPerfilForm(String titulo, JLabel preview, JTextField nome, JTextField email,
                                   JPasswordField password, JTextField imagem, JButton escolher,
                                   JLabel erro, JButton cancelar, JButton guardar,
                                   JComboBox<String> painelInicial, JLabel extraInfo) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 22, 18, 22));
        root.setPreferredSize(new Dimension(520, painelInicial != null ? 470 : 410));

        JLabel lblTitulo = label(titulo, 18f, null, Font.BOLD);

        JPanel topo = new JPanel(new BorderLayout(16, 0));
        topo.setOpaque(false);
        topo.add(preview, BorderLayout.WEST);
        topo.add(lblTitulo, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);

        int y = 0;
        c.gridy = y++; form.add(fieldGroup("Nome", nome), c);
        c.gridy = y++; form.add(fieldGroup("Email", email), c);
        c.gridy = y++; form.add(fieldGroup("Nova password", password), c);
        c.gridy = y++; form.add(fieldGroup("Imagem de perfil", imagem), c);

        JPanel linhaImagem = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaImagem.setOpaque(false);
        linhaImagem.add(escolher);
        c.gridy = y++; form.add(linhaImagem, c);

        if (painelInicial != null) {
            c.gridy = y++; form.add(fieldGroup("Painel inicial", painelInicial), c);
            if (extraInfo != null) {
                c.gridy = y++; form.add(extraInfo, c);
            }
        }

        c.gridy = y; form.add(erro, c);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(cancelar);
        acoes.add(guardar);

        root.add(topo, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(acoes, BorderLayout.SOUTH);
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
}
