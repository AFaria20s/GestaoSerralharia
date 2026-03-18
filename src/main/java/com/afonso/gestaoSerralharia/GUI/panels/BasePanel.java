package com.afonso.gestaoSerralharia.GUI.panels;

import com.afonso.gestaoSerralharia.GUI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {

    protected BasePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(
                UIConstants.PANEL_PAD_TOP,
                UIConstants.PANEL_PAD_SIDE,
                UIConstants.PANEL_PAD_BOTTOM,
                UIConstants.PANEL_PAD_SIDE));
    }

    protected JPanel buildHeader(String titulo, String subtitulo, JComponent acaoDireita) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, UIConstants.HEADER_MARGIN_BOTTOM, 0));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, UIConstants.FONT_PANEL_TITLE));

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(lblSub.getFont().deriveFont(UIConstants.FONT_PANEL_SUB));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        left.add(lblTitulo);
        left.add(lblSub);
        header.add(left, BorderLayout.WEST);
        if (acaoDireita != null) header.add(acaoDireita, BorderLayout.EAST);

        return header;
    }

    protected JPanel buildHeader(String titulo, String subtitulo) {
        return buildHeader(titulo, subtitulo, null);
    }

    protected JTextField buildSearchField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setPreferredSize(new Dimension(UIConstants.SEARCH_FIELD_WIDTH, UIConstants.SEARCH_FIELD_HEIGHT));
        return f;
    }

    protected JScrollPane buildTablePane(JTable table) {
        table.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        Color borderColor = UIManager.getColor("Component.borderColor");
        scroll.setBorder(BorderFactory.createLineBorder(
                borderColor != null ? borderColor : new Color(226, 232, 240)));
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
        btn.setFont(btn.getFont().deriveFont(UIConstants.FONT_SMALL));
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
