package com.afonso.gestaoSerralharia.GUI.windows;

import com.afonso.gestaoSerralharia.services.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

    private final AuthService authService;
    private boolean autenticado = false;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel erroLabel;

    public LoginDialog(AuthService authService) {
        super((Frame) null, "Serralharia — Entrar", true);
        this.authService = authService;
        setSize(400, 320);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));

        root.add(buildAccent(), BorderLayout.WEST);
        root.add(buildForm(),   BorderLayout.CENTER);

        add(root);
    }

    private JPanel buildAccent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(110, 0));
        panel.setBackground(new Color(30, 41, 59));

        JLabel icon  = new JLabel("⚙");
        JLabel name1 = new JLabel("Serra-");
        JLabel name2 = new JLabel("lharia");

        icon.setFont(icon.getFont().deriveFont(28f));
        icon.setForeground(new Color(148, 163, 184));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (JLabel l : new JLabel[]{name1, name2}) {
            l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
            l.setForeground(Color.WHITE);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(8));
        panel.add(name1);
        panel.add(name2);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(32, 28, 28, 28));
        panel.setBackground(UIManager.getColor("Panel.background"));

        JLabel titulo = new JLabel("Entrar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Bem-vindo de volta");
        subtitulo.setFont(subtitulo.getFont().deriveFont(12f));
        subtitulo.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setBorder(new EmptyBorder(3, 0, 22, 0));

        emailField    = new JTextField();
        passwordField = new JPasswordField();

        erroLabel = new JLabel(" ");
        erroLabel.setFont(erroLabel.getFont().deriveFont(11f));
        erroLabel.setForeground(new Color(220, 38, 38));
        erroLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnEntrar.setFocusPainted(false);
        btnEntrar.addActionListener((ActionEvent e) -> tentarLogin());

        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> tentarLogin());

        panel.add(titulo);
        panel.add(subtitulo);
        panel.add(buildField("Email", emailField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildField("Password", passwordField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(erroLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(btnEntrar);

        return panel;
    }

    private JPanel buildField(String label, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        group.add(lbl);
        group.add(field);
        return group;
    }

    private void tentarLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        erroLabel.setText(" ");

        if (email.isEmpty() || password.isEmpty()) {
            erroLabel.setText("Preenche todos os campos.");
            return;
        }

        boolean ok = authService.login(email, password);

        if (ok) {
            autenticado = true;
            dispose();
        } else {
            erroLabel.setText("Email ou password incorretos.");
            passwordField.setText("");
        }
    }

    public boolean isAutenticado() { return autenticado; }
}
