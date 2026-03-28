package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrcamentosPanel extends BasePanel {

    private final OrcamentoService         orcamentoService;
    private final LinhaorcamentoService    linhaorcamentoService;
    private final ObraService              obraService;
    private final TaxaivaService           taxaivaService;
    private final TipolinhaorcamentoService tipoService;

    private static final String[] COLUNAS = {"ID", "Obra", "Cliente", "Data", "Total s/IVA", "Total c/IVA", "Estado"};

    private DefaultTableModel modelo;
    private JTable            tabela;
    private JTextField        campoPesquisa;
    private JComboBox<String> filtroEstado;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OrcamentosPanel(OrcamentoService orcamentoService,
                           LinhaorcamentoService linhaorcamentoService,
                           ObraService obraService,
                           TaxaivaService taxaivaService,
                           TipolinhaorcamentoService tipoService) {
        this.orcamentoService      = orcamentoService;
        this.linhaorcamentoService = linhaorcamentoService;
        this.obraService           = obraService;
        this.taxaivaService        = taxaivaService;
        this.tipoService           = tipoService;

        JButton btnNovo = buildButton("Novo Orçamento");
        btnNovo.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFocusPainted(false);
        btnNovo.addActionListener(e -> abrirDialogoNovo());

        add(buildHeader("Orçamentos", "RF04 · RF05 · RF19 — elaborar, aprovar e exportar orçamentos", btnNovo),
                BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);

        carregarTabela();
    }

    // ── Corpo ─────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildBarraFiltros(), BorderLayout.NORTH);
        panel.add(buildAreaTabela(),   BorderLayout.CENTER);
        panel.add(buildBarraAcoes(),   BorderLayout.SOUTH);
        return panel;
    }

    // ── Filtros ───────────────────────────────────────────────────────────────

    private JPanel buildBarraFiltros() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por obra ou cliente…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { carregarTabela(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { carregarTabela(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { carregarTabela(); }
        });

        filtroEstado = new JComboBox<>(new String[]{"Todos", "Pendente", "Aprovado"});
        filtroEstado.setPreferredSize(new Dimension(130, UIConstants.SEARCH_FIELD_HEIGHT));
        filtroEstado.addActionListener(e -> carregarTabela());

        bar.add(campoPesquisa);
        bar.add(filtroEstado);
        return bar;
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

        // Ocultar ID
        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);

        tabela.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(90);

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDialogoEditar();
            }
        });

        return buildTablePane(tabela);
    }

    // ── Barra de ações ────────────────────────────────────────────────────────

    private JPanel buildBarraAcoes() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setOpaque(false);

        JButton btnVer    = buildSmallButton("Ver / Editar");
        JButton btnAprovar = buildSmallButton("Aprovar");
        JButton btnEliminar = buildSmallButton("Eliminar");

        btnAprovar.setForeground(UIConstants.COLOR_SUCCESS);
        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnVer.addActionListener(e -> abrirDialogoEditar());
        btnAprovar.addActionListener(e -> aprovarSelecionado());
        btnEliminar.addActionListener(e -> eliminarSelecionado());

        bar.add(btnVer);
        bar.add(btnAprovar);
        bar.add(btnEliminar);

        JLabel hint = new JLabel("  Duplo clique para abrir detalhes");
        hint.setFont(hint.getFont().deriveFont(UIConstants.FONT_SMALL));
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        bar.add(hint);

        return bar;
    }

    // ── Dados ─────────────────────────────────────────────────────────────────

    private void carregarTabela() {
        modelo.setRowCount(0);
        String filtro  = campoPesquisa != null ? campoPesquisa.getText().trim().toLowerCase() : "";
        String estado  = filtroEstado  != null ? (String) filtroEstado.getSelectedItem() : "Todos";

        List<Orcamento> lista = orcamentoService.listarTodos();

        for (Orcamento o : lista) {
            String nomeObra   = o.getIdObra() != null ? o.getIdObra().getDescricao() : "—";
            String nomeCliente = o.getIdObra() != null && o.getIdObra().getIdCliente() != null
                    ? o.getIdObra().getIdCliente().getNome() : "—";

            if (!filtro.isBlank() &&
                    !nomeObra.toLowerCase().contains(filtro) &&
                    !nomeCliente.toLowerCase().contains(filtro)) continue;

            boolean aprovado = Boolean.TRUE.equals(o.getAprovado());
            if ("Aprovado".equals(estado) && !aprovado) continue;
            if ("Pendente".equals(estado) &&  aprovado) continue;

            BigDecimal[] totais = calcularTotais(o);
            String semIva = totais[0].setScale(2, RoundingMode.HALF_UP) + " €";
            String comIva = totais[1].setScale(2, RoundingMode.HALF_UP) + " €";
            String estadoStr = aprovado ? "Aprovado" : "Pendente";
            String data = o.getDataEmissao() != null ? o.getDataEmissao().format(FMT) : "—";

            modelo.addRow(new Object[]{o.getId(), nomeObra, nomeCliente, data, semIva, comIva, estadoStr});
        }
    }

    private BigDecimal[] calcularTotais(Orcamento o) {
        List<Linhaorcamento> linhas = linhaorcamentoService.buscarPorOrcamento(o);
        BigDecimal semIva = BigDecimal.ZERO;
        BigDecimal comIva = BigDecimal.ZERO;
        for (Linhaorcamento l : linhas) {
            if (l.getPrecoUnit() == null || l.getQuantidade() == null) continue;
            BigDecimal subtotal = l.getPrecoUnit().multiply(l.getQuantidade());
            semIva = semIva.add(subtotal);
            BigDecimal pct = l.getIvaPercentagemAplicada() != null ? l.getIvaPercentagemAplicada() : BigDecimal.ZERO;
            comIva = comIva.add(subtotal.multiply(BigDecimal.ONE.add(pct.divide(BigDecimal.valueOf(100)))));
        }
        return new BigDecimal[]{semIva, comIva};
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    // ── Ações ─────────────────────────────────────────────────────────────────

    private void aprovarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "Aprovar este orçamento? A obra passará automaticamente para \"Em Execução\".",
                "Confirmar aprovação", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            orcamentoService.aprovar(id);
            carregarTabela();
            JOptionPane.showMessageDialog(this, "Orçamento aprovado.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void eliminarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "Eliminar este orçamento? Todas as linhas serão removidas.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            orcamentoService.eliminar(id);
            carregarTabela();
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void abrirDialogoNovo() {
        List<Obra> obras = obraService.listarTodos();
        if (obras.isEmpty()) {
            aviso("Não existem obras registadas. Cria uma obra primeiro.");
            return;
        }
        JDialog dlg = criarDialogo("Novo Orçamento");
        FormOrcamento form = new FormOrcamento(dlg, null, obras);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                Orcamento novo = form.construir();
                orcamentoService.guardar(novo);
                carregarTabela();
            } catch (Exception ex) {
                erro(ex.getMessage());
            }
        }
    }

    private void abrirDialogoEditar() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        Orcamento orc;
        try { orc = orcamentoService.buscarPorId(id); }
        catch (Exception ex) { erro(ex.getMessage()); return; }

        JDialog dlg = criarDialogo("Orçamento #" + orc.getId());
        DetalheOrcamento detalhe = new DetalheOrcamento(dlg, orc);
        dlg.add(detalhe);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        carregarTabela();
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private JDialog criarDialogo(String titulo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, titulo, true)
                : new JDialog((Dialog) owner, titulo, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);
        return dlg;
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    //  FORMULÁRIO — Novo Orçamento (seleção de obra)
    // =========================================================================

    private class FormOrcamento extends JPanel {
        private boolean confirmado = false;
        private final JComboBox<Obra> comboObra;

        FormOrcamento(JDialog dialogo, Orcamento orc, List<Obra> obras) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(400, 160));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(4, 0, 4, 0);
            c.weightx = 1.0;

            comboObra = new JComboBox<>(obras.toArray(new Obra[0]));
            comboObra.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object v,
                                                                        int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    if (v instanceof Obra obra) {
                        String cliente = obra.getIdCliente() != null ? obra.getIdCliente().getNome() : "—";
                        setText(obra.getDescricao() + "  (" + cliente + ")");
                    }
                    return this;
                }
            });

            JLabel lbl = new JLabel("Obra *");
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = 0; c.gridx = 0; corpo.add(lbl, c);
            c.gridy = 1; corpo.add(comboObra, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(12, 0, 0, 0));
            JButton btnC = buildButton("Cancelar");
            JButton btnG = buildButton("Criar Orçamento");
            btnG.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnG.setForeground(Color.WHITE);
            btnC.addActionListener(e -> dialogo.dispose());
            btnG.addActionListener(e -> { confirmado = true; dialogo.dispose(); });
            rodape.add(btnC);
            rodape.add(btnG);
            add(rodape, BorderLayout.SOUTH);
        }

        boolean confirmado() { return confirmado; }

        Orcamento construir() {
            Orcamento o = new Orcamento();
            o.setIdObra((Obra) comboObra.getSelectedItem());
            o.setDataEmissao(java.time.LocalDate.now());
            o.setAprovado(false);
            return o;
        }
    }

    // =========================================================================
    //  DETALHE / EDITOR DE ORÇAMENTO (com linhas)
    // =========================================================================

    private class DetalheOrcamento extends JPanel {

        private final JDialog    dialogo;
        private final Orcamento  orc;

        private DefaultTableModel modeloLinhas;
        private JTable            tabelaLinhas;

        private static final String[] COL_LINHAS = {"ID", "Descrição", "Tipo", "IVA %", "Qtd", "P. Unit.", "Total"};

        private JLabel lblTotal;

        DetalheOrcamento(JDialog dialogo, Orcamento orc) {
            this.dialogo = dialogo;
            this.orc     = orc;

            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(20, 24, 16, 24));
            setPreferredSize(new Dimension(820, 560));

            add(buildCabecalho(),  BorderLayout.NORTH);
            add(buildAreaLinhas(), BorderLayout.CENTER);
            add(buildRodape(),     BorderLayout.SOUTH);

            carregarLinhas();
        }

        // ── Cabeçalho informativo ─────────────────────────────────────────────

        private JPanel buildCabecalho() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(0, 0, 12, 0));

            // Linha de título
            JPanel topo = new JPanel(new BorderLayout());
            topo.setOpaque(false);

            String nomeObra   = orc.getIdObra() != null ? orc.getIdObra().getDescricao() : "—";
            String nomeCliente = orc.getIdObra() != null && orc.getIdObra().getIdCliente() != null
                    ? orc.getIdObra().getIdCliente().getNome() : "—";
            String data = orc.getDataEmissao() != null ? orc.getDataEmissao().format(FMT) : "—";

            JLabel lblObra = new JLabel(nomeObra);
            lblObra.setFont(lblObra.getFont().deriveFont(Font.BOLD, 15f));
            topo.add(lblObra, BorderLayout.WEST);

            boolean aprovado = Boolean.TRUE.equals(orc.getAprovado());
            JLabel badge = new JLabel(aprovado ? "  Aprovado  " : "  Pendente  ");
            badge.setOpaque(true);
            badge.setBackground(aprovado ? UIConstants.COLOR_SUCCESS : UIConstants.COLOR_WARNING);
            badge.setForeground(Color.WHITE);
            badge.setFont(badge.getFont().deriveFont(Font.BOLD, UIConstants.FONT_SMALL));
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));
            topo.add(badge, BorderLayout.EAST);

            panel.add(topo, BorderLayout.NORTH);

            // Meta info
            JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            meta.setOpaque(false);
            meta.add(metaLabel("Cliente", nomeCliente));
            meta.add(metaLabel("Data emissão", data));
            meta.add(metaLabel("Orçamento nº", String.valueOf(orc.getId())));
            panel.add(meta, BorderLayout.CENTER);

            // Separador
            JSeparator sep = new JSeparator();
            panel.add(sep, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel metaLabel(String chave, String valor) {
            JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
            p.setOpaque(false);
            JLabel k = new JLabel(chave);
            k.setFont(k.getFont().deriveFont(UIConstants.FONT_SMALL));
            k.setForeground(UIManager.getColor("Label.disabledForeground"));
            JLabel v = new JLabel(valor);
            v.setFont(v.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
            p.add(k);
            p.add(v);
            return p;
        }

        // ── Área de linhas ────────────────────────────────────────────────────

        private JPanel buildAreaLinhas() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(12, 0, 8, 0));

            // Toolbar de linhas
            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setOpaque(false);
            toolbar.setBorder(new EmptyBorder(0, 0, 6, 0));

            JLabel titulo = new JLabel("Linhas do orçamento");
            titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
            toolbar.add(titulo, BorderLayout.WEST);

            if (!Boolean.TRUE.equals(orc.getAprovado())) {
                JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
                acoes.setOpaque(false);
                JButton btnAdd = buildSmallButton("+ Adicionar linha");
                btnAdd.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
                btnAdd.setForeground(Color.WHITE);
                JButton btnRem = buildSmallButton("Remover linha");
                btnRem.setForeground(UIConstants.COLOR_DANGER);
                btnAdd.addActionListener(e -> abrirFormLinha(null));
                btnRem.addActionListener(e -> removerLinha());
                acoes.add(btnRem);
                acoes.add(btnAdd);
                toolbar.add(acoes, BorderLayout.EAST);
            }

            panel.add(toolbar, BorderLayout.NORTH);

            // Tabela de linhas
            modeloLinhas = new DefaultTableModel(COL_LINHAS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tabelaLinhas = new JTable(modeloLinhas);
            tabelaLinhas.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
            tabelaLinhas.setShowVerticalLines(false);
            tabelaLinhas.getTableHeader().setReorderingAllowed(false);
            tabelaLinhas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tabelaLinhas.setFillsViewportHeight(true);

            // Ocultar ID
            tabelaLinhas.getColumnModel().getColumn(0).setMinWidth(0);
            tabelaLinhas.getColumnModel().getColumn(0).setMaxWidth(0);
            tabelaLinhas.getColumnModel().getColumn(0).setWidth(0);

            tabelaLinhas.getColumnModel().getColumn(1).setPreferredWidth(220);
            tabelaLinhas.getColumnModel().getColumn(2).setPreferredWidth(90);
            tabelaLinhas.getColumnModel().getColumn(3).setPreferredWidth(60);
            tabelaLinhas.getColumnModel().getColumn(4).setPreferredWidth(55);
            tabelaLinhas.getColumnModel().getColumn(5).setPreferredWidth(80);
            tabelaLinhas.getColumnModel().getColumn(6).setPreferredWidth(90);

            if (!Boolean.TRUE.equals(orc.getAprovado())) {
                tabelaLinhas.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (e.getClickCount() == 2) abrirFormLinha(idLinhaSelecionada());
                    }
                });
            }

            JScrollPane scroll = buildTablePane(tabelaLinhas);
            scroll.setPreferredSize(new Dimension(760, 260));
            panel.add(scroll, BorderLayout.CENTER);

            // Totais
            JPanel totaisPanel = buildTotais();
            panel.add(totaisPanel, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel buildTotais() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
            panel.setOpaque(false);
            panel.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

            lblTotal = new JLabel();
            lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 13f));
            atualizarTotal();
            panel.add(lblTotal);
            return panel;
        }

        // ── Rodapé ────────────────────────────────────────────────────────────

        private JPanel buildRodape() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(8, 0, 0, 0));

            JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            esquerda.setOpaque(false);

            if (!Boolean.TRUE.equals(orc.getAprovado())) {
                JButton btnAprovar = buildButton("Aprovar Orçamento");
                btnAprovar.setBackground(UIConstants.COLOR_SUCCESS);
                btnAprovar.setForeground(Color.WHITE);
                btnAprovar.addActionListener(e -> {
                    int op = JOptionPane.showConfirmDialog(dialogo,
                            "Aprovar este orçamento? Não será possível editar as linhas após a aprovação.",
                            "Confirmar aprovação", JOptionPane.YES_NO_OPTION);
                    if (op == JOptionPane.YES_OPTION) {
                        try {
                            orcamentoService.aprovar(orc.getId());
                            dialogo.dispose();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                esquerda.add(btnAprovar);
            }

            panel.add(esquerda, BorderLayout.WEST);

            JButton btnFechar = buildButton("Fechar");
            btnFechar.addActionListener(e -> dialogo.dispose());
            JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            direita.setOpaque(false);
            direita.add(btnFechar);
            panel.add(direita, BorderLayout.EAST);

            return panel;
        }

        // ── Dados linhas ──────────────────────────────────────────────────────

        private void carregarLinhas() {
            modeloLinhas.setRowCount(0);
            List<Linhaorcamento> linhas = linhaorcamentoService.buscarPorOrcamento(orc);
            for (Linhaorcamento l : linhas) {
                String tipo = l.getIdTipoLinhaorcamento() != null ? l.getIdTipoLinhaorcamento().getNomeTipo() : "—";
                String iva  = l.getIvaPercentagemAplicada() != null
                        ? l.getIvaPercentagemAplicada().stripTrailingZeros().toPlainString() + "%" : "—";
                BigDecimal qtd = l.getQuantidade() != null ? l.getQuantidade() : BigDecimal.ONE;
                BigDecimal pu  = l.getPrecoUnit()  != null ? l.getPrecoUnit()  : BigDecimal.ZERO;
                BigDecimal pct = l.getIvaPercentagemAplicada() != null ? l.getIvaPercentagemAplicada() : BigDecimal.ZERO;
                BigDecimal total = qtd.multiply(pu).multiply(BigDecimal.ONE.add(pct.divide(BigDecimal.valueOf(100))));

                modeloLinhas.addRow(new Object[]{
                        l.getId(),
                        l.getNome() != null ? l.getNome() : "—",
                        tipo,
                        iva,
                        qtd.stripTrailingZeros().toPlainString(),
                        pu.setScale(2, RoundingMode.HALF_UP) + " €",
                        total.setScale(2, RoundingMode.HALF_UP) + " €"
                });
            }
            atualizarTotal();
        }

        private void atualizarTotal() {
            if (lblTotal == null) return;
            BigDecimal[] t = calcularTotais(orc);
            lblTotal.setText("Total s/IVA: " + t[0].setScale(2, RoundingMode.HALF_UP) + " €   " +
                    "Total c/IVA: " + t[1].setScale(2, RoundingMode.HALF_UP) + " €");
        }

        private Integer idLinhaSelecionada() {
            int row = tabelaLinhas.getSelectedRow();
            if (row < 0) return null;
            return (Integer) modeloLinhas.getValueAt(tabelaLinhas.convertRowIndexToModel(row), 0);
        }

        private void removerLinha() {
            Integer id = idLinhaSelecionada();
            if (id == null) { JOptionPane.showMessageDialog(dialogo, "Seleciona uma linha.", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
            int op = JOptionPane.showConfirmDialog(dialogo, "Remover esta linha?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                try { linhaorcamentoService.eliminar(id); carregarLinhas(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
            }
        }

        // ── Formulário de linha ───────────────────────────────────────────────

        private void abrirFormLinha(Integer idLinha) {
            Linhaorcamento existente = idLinha != null ? linhaorcamentoService.buscarPorId(idLinha) : null;

            Window owner = SwingUtilities.getWindowAncestor(dialogo);
            JDialog dlgLinha = owner instanceof Frame
                    ? new JDialog((Frame) owner, existente == null ? "Nova linha" : "Editar linha", true)
                    : new JDialog((Dialog) owner, existente == null ? "Nova linha" : "Editar linha", true);
            dlgLinha.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlgLinha.setResizable(false);

            FormLinha form = new FormLinha(dlgLinha, existente);
            dlgLinha.add(form);
            dlgLinha.pack();
            dlgLinha.setLocationRelativeTo(dialogo);
            dlgLinha.setVisible(true);

            if (form.confirmado()) {
                try {
                    Linhaorcamento linha = form.construir();
                    linha.setIdOrcamento(orc);
                    if (existente != null) linha.setId(existente.getId());
                    linhaorcamentoService.guardar(linha);
                    carregarLinhas();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    //  FORMULÁRIO DE LINHA
    // =========================================================================

    private class FormLinha extends JPanel {

        private boolean confirmado = false;

        private final JTextField   campoNome;
        private final JComboBox<Tipolinhaorcamento> comboTipo;
        private final JComboBox<Taxaiva>            comboIva;
        private final JTextField   campoQtd;
        private final JTextField   campoPreco;
        private final JLabel       lblErro;

        FormLinha(JDialog dialogo, Linhaorcamento existente) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(20, 24, 16, 24));
            setPreferredSize(new Dimension(440, 310));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            campoNome  = new JTextField();
            campoQtd   = new JTextField();
            campoPreco = new JTextField();

            List<Tipolinhaorcamento> tipos = tipoService.listarTodos();
            comboTipo = new JComboBox<>(tipos.toArray(new Tipolinhaorcamento[0]));
            comboTipo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object v,
                                                                        int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    if (v instanceof Tipolinhaorcamento t) setText(t.getNomeTipo());
                    return this;
                }
            });

            List<Taxaiva> taxas = taxaivaService.listarTodos();
            comboIva = new JComboBox<>(taxas.toArray(new Taxaiva[0]));
            comboIva.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object v,
                                                                        int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    if (v instanceof Taxaiva t) setText(t.getDescricao());
                    return this;
                }
            });

            if (existente != null) {
                campoNome.setText(existente.getNome() != null ? existente.getNome() : "");
                campoQtd.setText(existente.getQuantidade() != null
                        ? existente.getQuantidade().stripTrailingZeros().toPlainString() : "1");
                campoPreco.setText(existente.getPrecoUnit() != null
                        ? existente.getPrecoUnit().toPlainString() : "");
                if (existente.getIdTipoLinhaorcamento() != null) {
                    for (int i = 0; i < tipos.size(); i++) {
                        if (tipos.get(i).getId().equals(existente.getIdTipoLinhaorcamento().getId())) {
                            comboTipo.setSelectedIndex(i); break;
                        }
                    }
                }
                if (existente.getIdIva() != null) {
                    for (int i = 0; i < taxas.size(); i++) {
                        if (taxas.get(i).getId().equals(existente.getIdIva().getId())) {
                            comboIva.setSelectedIndex(i); break;
                        }
                    }
                }
            } else {
                campoQtd.setText("1");
            }

            int row = 0;
            addField(corpo, c, row++, "Descrição *", campoNome);
            addField(corpo, c, row++, "Tipo de linha", comboTipo);
            addField(corpo, c, row++, "Taxa de IVA", comboIva);
            addField(corpo, c, row++, "Quantidade *", campoQtd);
            addField(corpo, c, row++, "Preço unitário (€) *", campoPreco);

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2;
            c.insets = new Insets(6, 0, 0, 0);
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(12, 0, 0, 0));
            JButton btnC = buildButton("Cancelar");
            JButton btnG = buildButton(existente == null ? "Adicionar" : "Guardar");
            btnG.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnG.setForeground(Color.WHITE);
            btnC.addActionListener(e -> dialogo.dispose());
            btnG.addActionListener(e -> { if (validar()) { confirmado = true; dialogo.dispose(); } });
            rodape.add(btnC);
            rodape.add(btnG);
            add(rodape, BorderLayout.SOUTH);
        }

        private void addField(JPanel panel, GridBagConstraints c, int row, String label, JComponent campo) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(8, 0, 2, 0);
            panel.add(lbl, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            panel.add(campo, c);
        }

        private boolean validar() {
            if (campoNome.getText().trim().isBlank()) {
                lblErro.setText("A descrição é obrigatória."); return false;
            }
            try {
                BigDecimal qtd = new BigDecimal(campoQtd.getText().trim().replace(',', '.'));
                if (qtd.signum() <= 0) { lblErro.setText("Quantidade deve ser maior que zero."); return false; }
            } catch (NumberFormatException e) {
                lblErro.setText("Quantidade inválida."); return false;
            }
            try {
                BigDecimal preco = new BigDecimal(campoPreco.getText().trim().replace(',', '.'));
                if (preco.signum() <= 0) { lblErro.setText("Preço deve ser maior que zero."); return false; }
            } catch (NumberFormatException e) {
                lblErro.setText("Preço inválido."); return false;
            }
            lblErro.setText(" ");
            return true;
        }

        boolean confirmado() { return confirmado; }

        Linhaorcamento construir() {
            Linhaorcamento l = new Linhaorcamento();
            l.setNome(campoNome.getText().trim());
            l.setQuantidade(new BigDecimal(campoQtd.getText().trim().replace(',', '.')));
            l.setPrecoUnit(new BigDecimal(campoPreco.getText().trim().replace(',', '.')));
            if (comboTipo.getSelectedItem() instanceof Tipolinhaorcamento t) l.setIdTipoLinhaorcamento(t);
            if (comboIva.getSelectedItem() instanceof Taxaiva taxa) {
                l.setIdIva(taxa);
                l.setIvaPercentagemAplicada(taxa.getPercentagem());
            }
            return l;
        }
    }
}