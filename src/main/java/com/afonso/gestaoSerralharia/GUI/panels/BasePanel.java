package com.afonso.gestaoSerralharia.GUI.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {

    protected static final Color COLOR_DANGER  = new Color(220, 38, 38);
    protected static final Color COLOR_WARNING = new Color(217, 119, 6);
    protected static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    protected static final Color COLOR_INFO    = new Color(37, 99, 235);

    protected BasePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(28, 32, 28, 32));
    }

    protected JPanel buildHeader(String titulo, String subtitulo, JComponent acaoDireita) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel esquerda = new JPanel(new GridLayout(2, 1, 0, 3));
        esquerda.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 20f));

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(lblSub.getFont().deriveFont(12f));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        esquerda.add(lblTitulo);
        esquerda.add(lblSub);

        header.add(esquerda, BorderLayout.WEST);
        if (acaoDireita != null) header.add(acaoDireita, BorderLayout.EAST);

        return header;
    }

    protected JPanel buildHeader(String titulo, String subtitulo) {
        return buildHeader(titulo, subtitulo, null);
    }

    protected JTextField buildSearchField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setPreferredSize(new Dimension(220, 30));
        return f;
    }

    protected JScrollPane buildTablePane(JTable table) {
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor") != null
                        ? UIManager.getColor("Component.borderColor")
                        : new Color(226, 232, 240)));
        return scroll;
    }

    protected JButton buildButton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected JButton buildSmallButton(String texto) {
        JButton btn = buildButton(texto);
        btn.setFont(btn.getFont().deriveFont(11f));
        btn.setMargin(new Insets(2, 8, 2, 8));
        return btn;
    }

    protected JPanel buildEmptyState(String mensagem) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel(mensagem);
        lbl.setFont(lbl.getFont().deriveFont(13f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }
}
