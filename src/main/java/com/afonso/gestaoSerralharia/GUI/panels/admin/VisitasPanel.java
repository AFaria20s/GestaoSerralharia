package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Visita;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.VisitaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VisitasPanel extends BasePanel {

    // ── Serviços ─────────────────────────────────────────────────────────────
    private final VisitaService visitaService;
    private final ObraService   obraService;

    // ── UI ───────────────────────────────────────────────────────────────────
    private JTextField        campoPesquisa;
    private DefaultTableModel modelo;
    private JTable            tabela;

    // Colunas da tabela
    private static final String[] COLUNAS = {"ID", "Obra", "Data", "Notas (excerto)"};

    // Formato de apresentação de data
    private static final DateTimeFormatter FMT_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    // ─────────────────────────────────────────────────────────────────────────

    public VisitasPanel(VisitaService visitaService, ObraService obraService) {
        this.visitaService = visitaService;
        this.obraService   = obraService;

        // ── Botão "Nova Visita" no cabeçalho
        JButton btnNova = buildButton("+ Nova Visita");
        btnNova.putClientProperty("JButton.buttonType", "roundRect");
        btnNova.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNova.setForeground(Color.WHITE);
        btnNova.setFocusPainted(false);
        btnNova.addActionListener(e -> abrirDialogoNovo());

        add(buildHeader("Visitas",
                        "RF03 · RF18 — registar e consultar visitas a obras",
                        btnNova),
                BorderLayout.NORTH);

        add(buildCorpo(), BorderLayout.CENTER);

        carregarTabela(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CORPO PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildBarraPesquisa(), BorderLayout.NORTH);
        panel.add(buildAreaTabela(),    BorderLayout.CENTER);
        return panel;
    }

    // ── Barra de pesquisa ────────────────────────────────────────────────────

    private JPanel buildBarraPesquisa() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por obra (morada)…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        bar.add(campoPesquisa);
        return bar;
    }

    // ── Tabela ───────────────────────────────────────────────────────────────

    private JScrollPane buildAreaTabela() {
        modelo = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tabela.setShowVerticalLines(false);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);

        // Ocultar coluna ID
        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);

        // Larguras das colunas visíveis
        tabela.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(300);

        // Double-click para editar
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDialogoEditar();
            }
        });

        return buildTablePane(tabela);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DADOS
    // ─────────────────────────────────────────────────────────────────────────

    private void carregarTabela(String filtroObra) {
        modelo.setRowCount(0);

        List<Visita> lista = visitaService.listarTodos();

        for (Visita v : lista) {
            String obra = obraLabel(v.getIdObra());

            // Filtro por morada da obra (case-insensitive)
            if (filtroObra != null && !filtroObra.isBlank()) {
                if (!obra.toLowerCase().contains(filtroObra.toLowerCase())) continue;
            }

            String data   = v.getDataVisita() != null ? FMT_DATA.format(v.getDataVisita()) : "—";
            String notas  = excerto(v.getNotasMedicoes(), 60);

            modelo.addRow(new Object[]{v.getId(), obra, data, notas});
        }
    }

    private void filtrar() {
        carregarTabela(campoPesquisa.getText().trim());
    }

    /** Devolve o ID da visita na linha seleccionada, ou null. */
    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — NOVO
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDialogoNovo() {
        JDialog dlg = criarDialogo("Nova Visita");
        FormVisita form = new FormVisita(dlg, null);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                visitaService.guardar(form.construirVisita());
                carregarTabela(campoPesquisa.getText().trim());
                mostrarSucesso(dlg, "Visita registada com sucesso.");
            } catch (Exception ex) {
                mostrarErro(dlg, ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — EDITAR
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDialogoEditar() {
        Integer id = idSelecionado();
        if (id == null) return;

        Visita existente;
        try {
            existente = visitaService.buscarPorId(id);
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
            return;
        }

        JDialog dlg = criarDialogo("Editar Visita");
        FormVisita form = new FormVisita(dlg, existente);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                Visita atualizada = form.construirVisita();
                atualizada.setId(existente.getId());
                visitaService.guardar(atualizada);
                carregarTabela(campoPesquisa.getText().trim());
                mostrarSucesso(null, "Visita actualizada com sucesso.");
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELIMINAR (pode ser chamado externamente)
    // ─────────────────────────────────────────────────────────────────────────

    public void eliminarSelecionada() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona uma visita na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar esta visita?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                visitaService.eliminar(id);
                carregarTabela(campoPesquisa.getText().trim());
            } catch (Exception ex) {
                mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITÁRIOS
    // ─────────────────────────────────────────────────────────────────────────

    private JDialog criarDialogo(String titulo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, titulo, true)
                : new JDialog((Dialog) owner, titulo, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);
        return dlg;
    }

    private void mostrarSucesso(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /** Etiqueta legível para uma obra: "Rua X, nº Y — Localidade". */
    private static String obraLabel(Obra obra) {
        if (obra == null) return "—";
        return obra.getRua() + ", nº " + obra.getNporta() + " — " + obra.getLocalidade();
    }

    /** Trunca texto para exibição na tabela. */
    private static String excerto(String texto, int max) {
        if (texto == null || texto.isBlank()) return "—";
        String limpo = texto.replace('\n', ' ').trim();
        return limpo.length() <= max ? limpo : limpo.substring(0, max) + "…";
    }

    // =========================================================================
    //  FORMULÁRIO
    // =========================================================================

    /**
     * Painel de formulário reutilizável para criar e editar visitas.
     * Quando {@code visitaExistente} é null, opera em modo "Nova".
     *
     * Campos:
     *  – Obra        (JComboBox com todas as obras)
     *  – Data/hora   (JTextField, formato dd/MM/yyyy HH:mm)
     *  – Notas       (JTextArea com scroll)
     */
    private class FormVisita extends JPanel {

        private boolean confirmado = false;

        private final JComboBox<Obra> comboObra;
        private final JTextField      campoData;
        private final JTextArea       areaNotas;
        private final JLabel          lblErro;

        // Formato de entrada do utilizador
        private static final DateTimeFormatter FMT_INPUT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        FormVisita(JDialog dialogo, Visita visita) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(440, 360));

            // ── Corpo ─────────────────────────────────────────────────────
            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill    = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            // Obra
            List<Obra> obras = obraService.listarTodos();
            comboObra = new JComboBox<>(obras.toArray(new Obra[0]));
            comboObra.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object v,
                                                              int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    setText(v instanceof Obra ? obraLabel((Obra) v) : "—");
                    return this;
                }
            });

            // Data/hora
            campoData = new JTextField();
            campoData.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy HH:mm");

            // Notas
            areaNotas = new JTextArea(5, 20);
            areaNotas.setLineWrap(true);
            areaNotas.setWrapStyleWord(true);
            JScrollPane scrollNotas = new JScrollPane(areaNotas);
            scrollNotas.setBorder(BorderFactory.createLineBorder(
                    UIManager.getColor("Component.borderColor") != null
                            ? UIManager.getColor("Component.borderColor")
                            : new Color(226, 232, 240)));

            // Preencher se edição
            if (visita != null) {
                // Seleccionar obra correspondente
                for (int i = 0; i < obras.size(); i++) {
                    if (visita.getIdObra() != null &&
                            obras.get(i).getId().equals(visita.getIdObra().getId())) {
                        comboObra.setSelectedIndex(i);
                        break;
                    }
                }
                // Data
                if (visita.getDataVisita() != null) {
                    campoData.setText(FMT_INPUT.format(
                            visita.getDataVisita().atZone(ZoneId.systemDefault())));
                }
                // Notas
                if (visita.getNotasMedicoes() != null) {
                    areaNotas.setText(visita.getNotasMedicoes());
                }
            }

            // ── Adicionar campos ao GridBag ────────────────────────────
            int row = 0;
            adicionarCampo(corpo, c, row++, "Obra *",          comboObra);
            adicionarCampo(corpo, c, row++, "Data e hora *",   campoData);
            adicionarCampo(corpo, c, row++, "Notas / medições", scrollNotas);

            // Mensagem de erro
            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy    = row * 2;
            c.gridx    = 0;
            c.gridwidth = 2;
            c.insets   = new Insets(4, 0, 0, 0);
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            // ── Rodapé com botões ─────────────────────────────────────
            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 0, 0, 0));

            JButton btnCancelar = buildButton("Cancelar");
            JButton btnGuardar  = buildButton(visita == null ? "Registar" : "Guardar");
            btnGuardar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnGuardar.setForeground(Color.WHITE);

            btnCancelar.addActionListener(e -> dialogo.dispose());
            btnGuardar.addActionListener(e -> {
                if (validar()) {
                    confirmado = true;
                    dialogo.dispose();
                }
            });

            rodape.add(btnCancelar);
            rodape.add(btnGuardar);
            add(rodape, BorderLayout.SOUTH);
        }

        // ── Helpers de layout ──────────────────────────────────────────────

        private void adicionarCampo(JPanel panel, GridBagConstraints c,
                                    int row, String label, JComponent campo) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));

            // Label na linha par
            c.gridy     = row * 2;
            c.gridx     = 0;
            c.gridwidth = 2;
            c.insets    = new Insets(8, 0, 2, 0);
            panel.add(lbl, c);

            // Campo na linha ímpar seguinte
            c.gridy     = row * 2 + 1;
            c.gridx     = 0;
            c.gridwidth = 2;
            c.insets    = new Insets(0, 0, 4, 0);
            panel.add(campo, c);
        }

        // ── Validação ──────────────────────────────────────────────────────

        private boolean validar() {
            if (!(comboObra.getSelectedItem() instanceof Obra)) {
                lblErro.setText("Selecciona uma obra.");
                return false;
            }
            String dataTexto = campoData.getText().trim();
            if (dataTexto.isBlank()) {
                lblErro.setText("A data e hora são obrigatórias.");
                return false;
            }
            try {
                LocalDateTime.parse(dataTexto, FMT_INPUT);
            } catch (DateTimeParseException ex) {
                lblErro.setText("Formato de data inválido. Usa dd/MM/yyyy HH:mm.");
                return false;
            }
            lblErro.setText(" ");
            return true;
        }

        // ── API pública ────────────────────────────────────────────────────

        boolean confirmado() { return confirmado; }

        Visita construirVisita() {
            Visita v = new Visita();
            v.setIdObra((Obra) comboObra.getSelectedItem());

            LocalDateTime ldt = LocalDateTime.parse(campoData.getText().trim(), FMT_INPUT);
            v.setDataVisita(ldt.atZone(ZoneId.systemDefault()).toInstant());

            String notas = areaNotas.getText().trim();
            v.setNotasMedicoes(notas.isBlank() ? null : notas);

            return v;
        }
    }
}