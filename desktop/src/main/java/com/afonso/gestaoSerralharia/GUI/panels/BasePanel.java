package com.afonso.gestaoSerralharia.GUI.panels;

import com.afonso.gestaoSerralharia.GUI.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(226, 232, 242)),
                new EmptyBorder(0, 0, UIConstants.HEADER_MARGIN_BOTTOM - 6, 0)));

        boolean temSubtitulo = subtitulo != null && !subtitulo.isBlank();
        JPanel left = new JPanel(new GridLayout(temSubtitulo ? 2 : 1, 1, 0, 3));
        left.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, UIConstants.FONT_PANEL_TITLE));
        left.add(lblTitulo);

        if (temSubtitulo) {
            JLabel lblSub = new JLabel(subtitulo);
            lblSub.setFont(lblSub.getFont().deriveFont(UIConstants.FONT_PANEL_SUB));
            lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));
            left.add(lblSub);
        }
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
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor != null ? borderColor : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return scroll;
    }

    protected JButton buildButton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
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

    protected JPanel buildSurface(JComponent content, Insets padding) {
        JPanel surface = new JPanel(new BorderLayout());
        surface.setOpaque(true);
        surface.setBackground(Color.WHITE);
        surface.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 229, 239)),
                new EmptyBorder(padding)));
        surface.add(content, BorderLayout.CENTER);
        return surface;
    }

    public void onPanelShown() {
        String[] nomes = {"carregar", "carregarTabela", "refresh"};
        for (String nome : nomes) {
            try {
                var method = getClass().getDeclaredMethod(nome);
                method.setAccessible(true);
                method.invoke(this);
                break;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
                break;
            }
        }
        revalidate();
        repaint();
    }
}
