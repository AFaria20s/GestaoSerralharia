package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Gravidadeproblema;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Problema;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.services.GravidadeproblemaService;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.ProblemaService;
import com.afonso.gestaoSerralharia.services.TarefaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProblemasPanel extends BasePanel {

    // ── Serviços ──────────────────────────────────────────────────────────────
    private final ProblemaService         problemaService;
    private final ObraService             obraService;
    private final GravidadeproblemaService gravidadeService;
    private final TarefaService           tarefaService;

    // ── UI ────────────────────────────────────────────────────────────────────
    private JTextField            campoPesquisa;
    private JComboBox<String>     filtroGravidade;
    private JLabel                lblContador;
    private DefaultTableModel     modelo;
    private JTable                tabela;

    private List<Problema> problemasCarregados;

    private static final String[] COLUNAS = {"ID", "Obra", "Tarefa", "Gravidade", "Data", "Descrição"};
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    // ─────────────────────────────────────────────────────────────────────────

    public ProblemasPanel(ProblemaService problemaService,
                          ObraService obraService,
                          GravidadeproblemaService gravidadeService,
                          TarefaService tarefaService) {
        this.problemaService  = problemaService;
        this.obraService      = obraService;
        this.gravidadeService = gravidadeService;
        this.tarefaService    = tarefaService;

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);

        carregar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CABEÇALHO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JButton btnRefresh = buildButton("↻");
        btnRefresh.setToolTipText("Actualizar lista");
        btnRefresh.addActionListener(e -> carregar());

        JButton btnEliminar = buildButton("🗑 Eliminar");
        btnEliminar.setForeground(UIConstants.COLOR_DANGER);
        btnEliminar.addActionListener(e -> eliminarSelecionado());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(btnRefresh);
        acoes.add(btnEliminar);

        return buildHeader("Problemas",
                "Problemas reportados em obras em execução · RF05",
                acoes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CORPO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 12));
        corpo.setOpaque(false);
        corpo.add(buildBarraFiltros(), BorderLayout.NORTH);
        corpo.add(buildAreaTabela(),   BorderLayout.CENTER);
        return corpo;
    }

    // ── Barra de filtros ──────────────────────────────────────────────────────

    private JPanel buildBarraFiltros() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por descrição…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        // Filtro por gravidade — carregado dinamicamente
        filtroGravidade = new JComboBox<>();
        filtroGravidade.addItem("Todas as gravidades");
        gravidadeService.listarTodos()
                .forEach(g -> filtroGravidade.addItem(g.getNomeGravidade()));
        filtroGravidade.setPreferredSize(new Dimension(160, 30));
        filtroGravidade.addActionListener(e -> aplicarFiltro());

        lblContador = new JLabel("0 problemas");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.PLAIN, 12f));
        lblContador.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esquerda.setOpaque(false);
        esquerda.add(campoPesquisa);
        esquerda.add(filtroGravidade);
        esquerda.add(lblContador);

        barra.add(esquerda, BorderLayout.WEST);
        return barra;
    }

    // ── Tabela ────────────────────────────────────────────────────────────────

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

        // Larguras
        tabela.getColumnModel().getColumn(1).setPreferredWidth(200); // Obra
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160); // Tarefa
        tabela.getColumnModel().getColumn(3).setPreferredWidth(90);  // Gravidade
        tabela.getColumnModel().getColumn(4).setPreferredWidth(120); // Data
        tabela.getColumnModel().getColumn(5).setPreferredWidth(280); // Descrição

        // Badge de gravidade
        tabela.getColumnModel().getColumn(3).setCellRenderer(new GravidadeCellRenderer());

        // Double-click → detalhe
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDetalhe();
            }
        });

        return buildTablePane(tabela);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DADOS
    // ─────────────────────────────────────────────────────────────────────────

    private void carregar() {
        problemasCarregados = problemaService.listarTodos();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (problemasCarregados == null) return;

        String pesquisa  = campoPesquisa  != null ? campoPesquisa.getText().trim().toLowerCase() : "";
        String gravSel   = filtroGravidade != null ? (String) filtroGravidade.getSelectedItem()   : "Todas as gravidades";

        modelo.setRowCount(0);
        int count = 0;

        for (Problema p : problemasCarregados) {
            // Filtro gravidade
            if (!"Todas as gravidades".equals(gravSel)) {
                String gravNome = p.getIdGravidade() != null ? p.getIdGravidade().getNomeGravidade() : "";
                if (!gravNome.equalsIgnoreCase(gravSel)) continue;
            }
            // Filtro pesquisa
            if (!pesquisa.isEmpty()) {
                String desc = p.getDescricao() != null ? p.getDescricao().toLowerCase() : "";
                if (!desc.contains(pesquisa)) continue;
            }

            String obra    = obraLabel(p.getIdObra());
            String tarefa  = p.getIdTarefa() != null
                    ? "Tarefa #" + p.getIdTarefa().getId()
                    : "—";
            String grav    = p.getIdGravidade() != null ? p.getIdGravidade().getNomeGravidade() : "—";
            String data    = p.getDataReporte() != null ? FMT.format(p.getDataReporte()) : "—";
            String desc    = excerto(p.getDescricao(), 60);

            modelo.addRow(new Object[]{p.getId(), obra, tarefa, grav, data, desc});
            count++;
        }

        if (lblContador != null)
            lblContador.setText(count + (count == 1 ? " problema" : " problemas"));
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DETALHE
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDetalhe() {
        Integer id = idSelecionado();
        if (id == null) return;

        Problema p;
        try { p = problemaService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(null, ex.getMessage()); return; }

        JDialog dlg = criarDialogo("Detalhe do Problema");
        dlg.setSize(500, 380);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(24, 28, 20, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        // Título + badge gravidade
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Problema #" + p.getId());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));

        String gravNome = p.getIdGravidade() != null ? p.getIdGravidade().getNomeGravidade() : "—";
        JLabel badge = buildBadgeGravidade(gravNome);

        topo.add(lblTitulo, BorderLayout.WEST);
        topo.add(badge,     BorderLayout.EAST);

        // Corpo com campos
        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 0, 6, 16);
        gbc.anchor  = GridBagConstraints.NORTHWEST;

        String obra   = obraLabel(p.getIdObra());
        String tarefa = p.getIdTarefa() != null ? "Tarefa #" + p.getIdTarefa().getId() : "—";
        String data   = p.getDataReporte() != null ? FMT.format(p.getDataReporte()) : "—";
        String desc   = p.getDescricao() != null ? p.getDescricao() : "—";

        addDetalheRow(corpo, gbc, 0, "Obra",      obra);
        addDetalheRow(corpo, gbc, 1, "Tarefa",    tarefa);
        addDetalheRow(corpo, gbc, 2, "Gravidade", gravNome);
        addDetalheRow(corpo, gbc, 3, "Reportado", data);

        // Descrição completa (área de texto read-only)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lblDesc = new JLabel("Descrição:");
        lblDesc.setFont(lblDesc.getFont().deriveFont(Font.BOLD, 12f));
        lblDesc.setForeground(UIManager.getColor("Label.disabledForeground"));
        corpo.add(lblDesc, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea taDesc = new JTextArea(desc);
        taDesc.setEditable(false);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        taDesc.setFont(taDesc.getFont().deriveFont(13f));
        taDesc.setBackground(UIManager.getColor("Panel.background"));
        taDesc.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor") != null
                        ? UIManager.getColor("Component.borderColor") : new Color(226, 232, 240)));
        corpo.add(new JScrollPane(taDesc), gbc);

        // Fechar
        JButton btnFechar = buildButton("Fechar");
        btnFechar.addActionListener(e -> dlg.dispose());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        rodape.add(btnFechar);

        content.add(topo,   BorderLayout.NORTH);
        content.add(corpo,  BorderLayout.CENTER);
        content.add(rodape, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELIMINAR
    // ─────────────────────────────────────────────────────────────────────────

    public void eliminarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona um problema na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar este problema?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                problemaService.eliminar(id);
                carregar();
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

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private static String obraLabel(Obra obra) {
        if (obra == null) return "—";
        return obra.getRua() + ", nº " + obra.getNporta() + " — " + obra.getLocalidade();
    }

    private static String excerto(String texto, int max) {
        if (texto == null || texto.isBlank()) return "—";
        String limpo = texto.replace('\n', ' ').trim();
        return limpo.length() <= max ? limpo : limpo.substring(0, max) + "…";
    }

    private void addDetalheRow(JPanel panel, GridBagConstraints gbc,
                               int row, String label, String valor) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setPreferredSize(new Dimension(90, 24));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JLabel val = new JLabel(valor);
        val.setFont(val.getFont().deriveFont(Font.PLAIN, 13f));
        panel.add(val, gbc);
        gbc.weightx = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BADGE GRAVIDADE
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel buildBadgeGravidade(String gravidade) {
        Color cor = corGravidade(gravidade);
        JLabel badge = new JLabel(gravidade) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setForeground(Color.WHITE);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 11f));
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        return badge;
    }

    static Color corGravidade(String gravidade) {
        if (gravidade == null) return Color.GRAY;
        return switch (gravidade.toLowerCase()) {
            case "crítica", "critica", "alta"  -> UIConstants.COLOR_DANGER;
            case "média", "media"              -> UIConstants.COLOR_WARNING;
            case "baixa"                       -> UIConstants.COLOR_SUCCESS;
            default                            -> UIConstants.COLOR_INFO;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RENDERER — badge de gravidade na tabela
    // ─────────────────────────────────────────────────────────────────────────

    private static class GravidadeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));

            if (!sel && v != null) {
                Color bg = corGravidade(v.toString());
                // versão pastel para a tabela
                lbl.setBackground(bg.brighter().brighter());
                lbl.setForeground(bg.darker().darker());
            }
            return lbl;
        }
    }
}