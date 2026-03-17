package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.services.CargoService;
import com.afonso.gestaoSerralharia.services.DonoService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Janela de autenticação com dois painéis: Login e Registo.
 * Troca entre painéis com animação de fade via Timer.
 */
public class AuthFrame extends JDialog {

    // ── Serviços ──────────────────────────────────────────────────────────────
    private final DonoService donoService;
    private final FuncionarioService funcionarioService;
    private final CargoService cargoService;

    // ── Estado ────────────────────────────────────────────────────────────────
    private boolean autenticado = false;

    // ── Layout ────────────────────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel cardContainer;

    // ── Painéis ───────────────────────────────────────────────────────────────
    private JPanel loginPanel;
    private JPanel registerPanel;

    // ── Campos Login ──────────────────────────────────────────────────────────
    private JTextField loginEmailField;
    private JPasswordField loginPassField;
    private JLabel loginErroLabel;

    // ── Campos Registo ────────────────────────────────────────────────────────
    private JTextField regNomeField;
    private JTextField regEmailField;
    private JPasswordField regPassField;
    private JPasswordField regPassConfirmField;
    private JComboBox<String> regTipoCombo;
    private JComboBox<Cargo> regCargoCombo;
    private JLabel regCargoLabel;
    private JLabel regErroLabel;

    // ─────────────────────────────────────────────────────────────────────────
    public AuthFrame(DonoService donoService, FuncionarioService funcionarioService, CargoService cargoService) {
        super((Frame) null, "Serralharia — Autenticação", true); // true = modal
        this.donoService = donoService;
        this.funcionarioService = funcionarioService;
        this.cargoService = cargoService;

        setSize(440, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        buildUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CONSTRUÇÃO DA UI
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));

        // Painel esquerdo decorativo (faixa colorida)
        JPanel accent = buildAccentPanel();

        // Container com os dois cards
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(UIManager.getColor("Panel.background"));

        loginPanel   = buildLoginPanel();
        registerPanel = buildRegisterPanel();

        cardContainer.add(loginPanel,    "login");
        cardContainer.add(registerPanel, "register");

        root.add(accent,        BorderLayout.WEST);
        root.add(cardContainer, BorderLayout.CENTER);

        add(root);
    }

    /** Faixa vertical à esquerda com logo e mensagem */
    private JPanel buildAccentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(130, 0));
        panel.setBackground(new Color(30, 41, 59)); // slate-800

        panel.add(Box.createVerticalGlue());

        JLabel icon = new JLabel("⚙");
        icon.setFont(icon.getFont().deriveFont(36f));
        icon.setForeground(new Color(148, 163, 184)); // slate-400
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Serra-");
        JLabel titulo2 = new JLabel("lharia");
        for (JLabel l : new JLabel[]{titulo, titulo2}) {
            l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
            l.setForeground(Color.WHITE);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        JLabel sub = new JLabel("<html><center>Gestão<br>de Obras</center></html>");
        sub.setFont(sub.getFont().deriveFont(11f));
        sub.setForeground(new Color(148, 163, 184));
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(6, 8, 0, 8));

        panel.add(icon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titulo);
        panel.add(titulo2);
        panel.add(sub);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PAINEL DE LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setBorder(new EmptyBorder(40, 32, 32, 32));

        // Cabeçalho
        JLabel titulo = new JLabel("Entrar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Bem-vindo de volta");
        sub.setFont(sub.getFont().deriveFont(13f));
        sub.setForeground(UIManager.getColor("Label.disabledForeground"));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 28, 0));

        // Campos
        loginEmailField = new JTextField();
        loginPassField  = new JPasswordField();

        // Erro
        loginErroLabel = buildErroLabel();

        // Botão entrar
        JButton btnEntrar = buildPrimaryButton("Entrar");
        btnEntrar.addActionListener(e -> tentarLogin());
        loginPassField.addActionListener(e -> tentarLogin());
        loginEmailField.addActionListener(e -> loginPassField.requestFocusInWindow());

        // Link para registo
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel txtLink = new JLabel("Não tens conta?  ");
        txtLink.setFont(txtLink.getFont().deriveFont(12f));
        txtLink.setForeground(UIManager.getColor("Label.disabledForeground"));
        JButton btnIrRegisto = buildLinkButton("Criar conta");
        btnIrRegisto.addActionListener(e -> switchTo("register"));
        linkRow.add(txtLink);
        linkRow.add(btnIrRegisto);

        // Montagem
        panel.add(titulo);
        panel.add(sub);
        panel.add(buildFieldGroup("Email", loginEmailField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildFieldGroup("Password", loginPassField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(loginErroLabel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(btnEntrar);
        panel.add(Box.createVerticalStrut(14));
        panel.add(linkRow);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PAINEL DE REGISTO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setBorder(new EmptyBorder(32, 32, 24, 32));

        // Cabeçalho
        JLabel titulo = new JLabel("Criar conta");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Preenche os dados abaixo");
        sub.setFont(sub.getFont().deriveFont(13f));
        sub.setForeground(UIManager.getColor("Label.disabledForeground"));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));

        // Campos
        regNomeField        = new JTextField();
        regEmailField       = new JTextField();
        regPassField        = new JPasswordField();
        regPassConfirmField = new JPasswordField();

        // Tipo de conta
        regTipoCombo = new JComboBox<>(new String[]{"Administrador (Dono)", "Funcionário"});
        regTipoCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        regTipoCombo.addActionListener(e -> updateCargoVisibility());

        // Cargo (só visível para Funcionário)
        regCargoLabel = new JLabel("Cargo");
        regCargoLabel.setFont(regCargoLabel.getFont().deriveFont(12f));
        regCargoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        regCargoLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        regCargoCombo = new JComboBox<>();
        regCargoCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        regCargoCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        loadCargos();

        // Erro
        regErroLabel = buildErroLabel();

        // Botão registar
        JButton btnRegistar = buildPrimaryButton("Criar conta");
        btnRegistar.addActionListener(e -> tentarRegistar());

        // Link para login
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel txtLink = new JLabel("Já tens conta?  ");
        txtLink.setFont(txtLink.getFont().deriveFont(12f));
        txtLink.setForeground(UIManager.getColor("Label.disabledForeground"));
        JButton btnIrLogin = buildLinkButton("Entrar");
        btnIrLogin.addActionListener(e -> switchTo("login"));
        linkRow.add(txtLink);
        linkRow.add(btnIrLogin);

        // Montagem
        panel.add(titulo);
        panel.add(sub);
        panel.add(buildFieldGroup("Nome completo", regNomeField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildFieldGroup("Email", regEmailField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildFieldGroup("Password", regPassField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildFieldGroup("Confirmar password", regPassConfirmField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildFieldGroup("Tipo de conta", regTipoCombo));
        panel.add(Box.createVerticalStrut(10));
        panel.add(regCargoLabel);
        panel.add(regCargoCombo);
        panel.add(Box.createVerticalStrut(8));
        panel.add(regErroLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btnRegistar);
        panel.add(Box.createVerticalStrut(12));
        panel.add(linkRow);

        // Cargo começa escondido (default = Administrador)
        updateCargoVisibility();

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LÓGICA
    // ─────────────────────────────────────────────────────────────────────────

    private void tentarLogin() {
        String email    = loginEmailField.getText().trim();
        String password = new String(loginPassField.getPassword());
        loginErroLabel.setText(" ");

        if (email.isEmpty() || password.isEmpty()) {
            loginErroLabel.setText("Preenche todos os campos.");
            return;
        }

        // Tenta como Dono
        try {
            Dono dono = donoService.autenticar(email, password);
            SessionManager.getInstance().loginDono(dono);
            autenticado = true;
            dispose();
            return;
        } catch (Exception ignored) {}

        // Tenta como Funcionário
        try {
            Funcionario func = funcionarioService.autenticar(email, password);
            SessionManager.getInstance().loginFuncionario(func);
            autenticado = true;
            dispose();
            return;
        } catch (Exception ignored) {}

        loginErroLabel.setText("Email ou password incorretos.");
        loginPassField.setText("");
    }

    private void tentarRegistar() {
        regErroLabel.setText(" ");

        String nome          = regNomeField.getText().trim();
        String email         = regEmailField.getText().trim();
        String pass          = new String(regPassField.getPassword());
        String passConfirm   = new String(regPassConfirmField.getPassword());
        boolean isAdmin      = regTipoCombo.getSelectedIndex() == 0;

        // Validações
        if (nome.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            regErroLabel.setText("Preenche todos os campos obrigatórios.");
            return;
        }
        if (!email.contains("@")) {
            regErroLabel.setText("Email inválido.");
            return;
        }
        if (pass.length() < 6) {
            regErroLabel.setText("A password deve ter pelo menos 6 caracteres.");
            return;
        }
        if (!pass.equals(passConfirm)) {
            regErroLabel.setText("As passwords não coincidem.");
            regPassConfirmField.setText("");
            return;
        }
        if (!isAdmin && regCargoCombo.getSelectedItem() == null) {
            regErroLabel.setText("Seleciona um cargo.");
            return;
        }

        try {
            if (isAdmin) {
                Dono dono = new Dono();
                dono.setNome(nome);
                dono.setEmail(email);
                dono.setPassword(pass);
                Dono saved = donoService.guardar(dono);
                SessionManager.getInstance().loginDono(saved);
            } else {
                Funcionario func = new Funcionario();
                func.setNome(nome);
                func.setEmail(email);
                func.setPassword(pass);
                func.setIdCargo((Cargo) regCargoCombo.getSelectedItem());
                Funcionario saved = funcionarioService.guardar(func);
                SessionManager.getInstance().loginFuncionario(saved);
            }
            autenticado = true;
            dispose();

        } catch (IllegalArgumentException ex) {
            regErroLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            regErroLabel.setText("Erro ao criar conta. Tenta novamente.");
        }
    }

    /** Mostra/esconde o campo de cargo consoante o tipo selecionado */
    private void updateCargoVisibility() {
        boolean isFuncionario = regTipoCombo.getSelectedIndex() == 1;
        regCargoLabel.setVisible(isFuncionario);
        regCargoCombo.setVisible(isFuncionario);

        // Ajusta altura da janela consoante visibilidade
        setSize(440, isFuncionario ? 620 : 560);
        setLocationRelativeTo(null);
        revalidate();
    }

    /** Carrega cargos da BD para o combo */
    private void loadCargos() {
        try {
            List<Cargo> cargos = cargoService.listarTodos();
            for (Cargo c : cargos) regCargoCombo.addItem(c);

            // Renderer para mostrar o nome do cargo
            regCargoCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Cargo) setText(((Cargo) value).getNome());
                    return this;
                }
            });
        } catch (Exception e) {
            regCargoCombo.addItem(null);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ANIMAÇÃO DE TRANSIÇÃO ENTRE PAINÉIS
    // ─────────────────────────────────────────────────────────────────────────

    private void switchTo(String target) {
        // Fade out → troca → fade in usando Timer
        Timer fadeOut = new Timer(15, null);
        final float[] alpha = {1.0f};

        fadeOut.addActionListener(e -> {
            alpha[0] -= 0.08f;
            if (alpha[0] <= 0f) {
                alpha[0] = 0f;
                fadeOut.stop();
                cardLayout.show(cardContainer, target);
                // limpar erros e campos ao mudar de painel
                resetFields(target);
                fadeIn();
            }
            cardContainer.setOpaque(false);
            setOpacity(Math.max(0.5f, alpha[0]));
        });
        fadeOut.start();
    }

    private void fadeIn() {
        Timer fadeIn = new Timer(15, null);
        final float[] alpha = {0.5f};

        fadeIn.addActionListener(e -> {
            alpha[0] += 0.08f;
            if (alpha[0] >= 1f) {
                alpha[0] = 1f;
                fadeIn.stop();
            }
            setOpacity(alpha[0]);
        });
        fadeIn.start();
    }

    private void resetFields(String target) {
        if (target.equals("login")) {
            loginPassField.setText("");
            loginErroLabel.setText(" ");
        } else {
            regNomeField.setText("");
            regEmailField.setText("");
            regPassField.setText("");
            regPassConfirmField.setText("");
            regErroLabel.setText(" ");
            regTipoCombo.setSelectedIndex(0);
            updateCargoVisibility();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS DE COMPONENTES
    // ─────────────────────────────────────────────────────────────────────────

    /** Grupo de label + campo alinhado */
    private JPanel buildFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        group.add(lbl);
        group.add(field);
        return group;
    }

    private JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setForeground(UIManager.getColor("Component.accentColor") != null
                ? UIManager.getColor("Component.accentColor")
                : new Color(59, 130, 246));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        return btn;
    }

    private JLabel buildErroLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(lbl.getFont().deriveFont(11f));
        lbl.setForeground(new Color(220, 38, 38)); // red-600
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    public boolean isAutenticado() { return autenticado; }
}
