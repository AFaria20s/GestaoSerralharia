package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class FaturacaoPanel extends BasePanel {

    private final FaturaService          faturaService;
    private final ObraService            obraService;
    private final OrcamentoService       orcamentoService;
    private final LinhaorcamentoService  linhaorcamentoService;
    private final EstadopagamentoService estadopagamentoService;

    private DefaultTableModel modelo;
    private JTable            tabela;
    private JComboBox<String> filtroEstado;

    private static final String[] COLUNAS = {
            "ID", "Documento", "Parcela", "Obra", "Cliente", "Data Emissão", "Vencimento",
            "Base s/IVA", "IVA", "Total c/IVA", "Pago", "Saldo", "Estado"
    };
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FaturacaoPanel(FaturaService faturaService,
                          ObraService obraService,
                          OrcamentoService orcamentoService,
                          LinhaorcamentoService linhaorcamentoService,
                          EstadopagamentoService estadopagamentoService) {
        this.faturaService          = faturaService;
        this.obraService            = obraService;
        this.orcamentoService       = orcamentoService;
        this.linhaorcamentoService  = linhaorcamentoService;
        this.estadopagamentoService = estadopagamentoService;

        JButton btnEmitir = buildButton("Emitir Fatura");
        btnEmitir.putClientProperty("JButton.buttonType", "roundRect");
        btnEmitir.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnEmitir.setForeground(Color.WHITE);
        btnEmitir.addActionListener(e -> abrirDialogoEmitir());

        add(buildHeader("Faturação", "", btnEmitir), BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);
        carregarTabela(null);
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildSurface(buildBarra(), new Insets(10, 12, 10, 12)), BorderLayout.NORTH);
        panel.add(buildAreaTabela(), BorderLayout.CENTER);
        panel.add(buildSurface(buildRodape(), new Insets(10, 12, 10, 12)), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBarra() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        LinkedHashSet<String> opcoesEstado = new LinkedHashSet<>();
        opcoesEstado.add("Todos");
        for (Estadopagamento estado : estadopagamentoService.listarTodos()) {
            if (estado.getNomeEstado() != null && !estado.getNomeEstado().isBlank()) {
                opcoesEstado.add(estado.getNomeEstado());
            }
        }
        opcoesEstado.add("Vencida");

        filtroEstado = new JComboBox<>(opcoesEstado.toArray(new String[0]));
        filtroEstado.addActionListener(e -> carregarTabela((String) filtroEstado.getSelectedItem()));

        JLabel lbl = new JLabel("Estado:");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
        bar.add(lbl);
        bar.add(filtroEstado);
        return bar;
    }

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

        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(65);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(85);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(10).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(11).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(12).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(12).setCellRenderer(new EstadoPagamentoRenderer());

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDetalhe();
            }
        });

        return buildTablePane(tabela);
    }

    private JPanel buildRodape() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setOpaque(false);

        JButton btnPagar    = buildSmallButton("Registar Pagamento");
        JButton btnDetalhe  = buildSmallButton("Ver Detalhe");
        JButton btnEliminar = buildSmallButton("Eliminar");

        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnPagar.addActionListener(e -> abrirDialogoPagamento());
        btnDetalhe.addActionListener(e -> abrirDetalhe());
        btnEliminar.addActionListener(e -> eliminarSelecionada());

        JLabel hint = new JLabel("  Duplo clique para ver detalhe");
        hint.setFont(hint.getFont().deriveFont(UIConstants.FONT_SMALL));
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));

        bar.add(btnPagar);
        bar.add(btnDetalhe);
        bar.add(btnEliminar);
        bar.add(hint);
        return bar;
    }

    // ── Dados ─────────────────────────────────────────────────────────────────

    private void carregarTabela(String filtroNomeEstado) {
        modelo.setRowCount(0);
        for (Fatura f : faturaService.listarTodos()) {
            String estado = faturaService.estadoApresentacao(f);

            if (filtroNomeEstado != null && !filtroNomeEstado.equals("Todos")
                    && !filtroNomeEstado.equals(estado)) continue;

            String obra    = f.getIdObra() != null ? nomeObra(f.getIdObra()) : "—";
            String cliente = f.getClienteNome() != null && !f.getClienteNome().isBlank()
                    ? f.getClienteNome()
                    : (f.getIdObra() != null && f.getIdObra().getIdCliente() != null
                       ? f.getIdObra().getIdCliente().getNome() : "—");
            String data       = f.getDataEmissao()    != null ? f.getDataEmissao().format(FMT)    : "—";
            String vencimento = f.getDataVencimento() != null ? f.getDataVencimento().format(FMT) : "—";
            String base       = euros(faturaService.valorSubtotalSemIva(f));
            String iva        = euros(faturaService.valorIva(f));
            String total      = euros(f.getValorTotalComIva());
            String pago       = euros(f.getValorPago());
            String saldo      = euros(faturaService.saldoEmDivida(f));
            String documento  = f.getCodigoDocumento() != null && !f.getCodigoDocumento().isBlank()
                    ? f.getCodigoDocumento() : "FT-" + f.getId();

            modelo.addRow(new Object[]{
                    f.getId(), documento,
                    f.getNumeroParcela() != null ? f.getNumeroParcela() : 1,
                    obra, cliente, data, vencimento,
                    base, iva, total, pago, saldo, estado
            });
        }
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    // ── Helpers estáticos ─────────────────────────────────────────────────────

    private static String nomeObra(Obra o) {
        String d = o.getDescricao();
        return (d != null && !d.isBlank()) ? d : "Obra #" + o.getId();
    }

    private static String euros(BigDecimal v) {
        return v != null ? String.format("%.2f €", v) : "—";
    }

    private static String textoOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor.trim();
    }

    private static String codigoOuFallback(Fatura fatura) {
        return fatura.getCodigoDocumento() != null && !fatura.getCodigoDocumento().isBlank()
                ? fatura.getCodigoDocumento() : "FT-" + fatura.getId();
    }

    private static String prazoPagamentoTexto(Orcamento orcamento) {
        int prazo = orcamento != null
                && orcamento.getPrazoPagamentoDias() != null
                && orcamento.getPrazoPagamentoDias() > 0
                ? orcamento.getPrazoPagamentoDias() : 30;
        return prazo + " dias";
    }

    // ── Emitir fatura ─────────────────────────────────────────────────────────

    private void abrirDialogoEmitir() {
        List<Obra> candidatas = new ArrayList<>(
                orcamentoService.buscarAprovados().stream()
                        .map(Orcamento::getIdObra)
                        .filter(o -> o != null && o.getId() != null)
                        .filter(o -> orcamentoService.buscarAprovadoPorObra(o)
                                .map(orc -> faturaService.saldoPorFaturar(o, orc).compareTo(BigDecimal.ZERO) > 0)
                                .orElse(false))
                        .collect(java.util.stream.Collectors.toMap(
                                Obra::getId, o -> o, (a, b) -> a, LinkedHashMap::new))
                        .values());

        if (candidatas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não existem obras com orçamento aprovado e valor ainda por faturar.\n" +
                            "Aprova um orçamento em 'Orçamentos' para poder faturar.",
                    "Sem obras disponíveis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = criarDialogo("Emitir Fatura");
        EmitirFaturaForm form = new EmitirFaturaForm(dlg, candidatas);
        dlg.add(form);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(480, 420));
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                faturaService.emitir(form.obraEscolhida(), form.valorEmitir(), form.descricao());
                carregarTabela((String) filtroEstado.getSelectedItem());
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    // ── Registar pagamento ────────────────────────────────────────────────────

    private void abrirDialogoPagamento() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma fatura na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Fatura fatura;
        try { fatura = faturaService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(null, ex.getMessage()); return; }

        BigDecimal saldo = faturaService.saldoEmDivida(fatura);
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Esta fatura já está totalmente paga.",
                    "Fatura paga", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = criarDialogo("Registar Pagamento");
        PagamentoForm form = new PagamentoForm(dlg, fatura, saldo);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                faturaService.registarPagamento(id, form.valorPago(), form.dataPagamento(),
                        form.meioPagamento(), form.referenciaPagamento(), form.observacoes());
                carregarTabela((String) filtroEstado.getSelectedItem());
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    // ── Detalhe ───────────────────────────────────────────────────────────────

    private void abrirDetalhe() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma fatura na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Fatura fatura;
        try { fatura = faturaService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(null, ex.getMessage()); return; }

        JDialog dlg = criarDialogo("Detalhe da Fatura — " + codigoOuFallback(fatura));
        DetalhePanel detalhe = new DetalhePanel(fatura);
        JScrollPane scroll = new JScrollPane(detalhe);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.setContentPane(scroll);
        dlg.setSize(new Dimension(860, 720));
        dlg.setMinimumSize(new Dimension(760, 580));
        dlg.setResizable(true);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    private void eliminarSelecionada() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Seleciona uma fatura na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar esta fatura?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            try {
                faturaService.eliminar(id);
                carregarTabela((String) filtroEstado.getSelectedItem());
            } catch (Exception ex) {
                mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
            }
        }
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

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ── Renderer de estado ────────────────────────────────────────────────────

    private static class EstadoPagamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            String estado = value != null ? value.toString() : "";
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                if ("Pago".equalsIgnoreCase(estado)) {
                    lbl.setBackground(new Color(220, 252, 231));
                    lbl.setForeground(new Color(22, 101, 52));
                } else if ("Parcial".equalsIgnoreCase(estado)) {
                    lbl.setBackground(new Color(254, 249, 195));
                    lbl.setForeground(new Color(133, 77, 14));
                } else if ("Vencida".equalsIgnoreCase(estado)) {
                    lbl.setBackground(new Color(254, 226, 226));
                    lbl.setForeground(new Color(153, 27, 27));
                } else {
                    lbl.setBackground(new Color(255, 237, 213));
                    lbl.setForeground(new Color(154, 52, 18));
                }
            }
            return lbl;
        }
    }

    // =========================================================================
    //  FORMULÁRIO – Emitir fatura
    // =========================================================================

    private class EmitirFaturaForm extends JPanel {

        private boolean confirmado = false;
        private final JComboBox<Obra> comboObra;
        private final JTextArea       areaPreview;
        private final JTextField      campoValor;
        private final JTextField      campoDescricao;
        private final JLabel          lblErro;

        EmitirFaturaForm(JDialog dialogo, List<Obra> obras) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            comboObra = new JComboBox<>(obras.toArray(new Obra[0]));
            comboObra.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(
                        JList<?> list, Object v, int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    if (v instanceof Obra o) {
                        String cli = o.getIdCliente() != null ? " — " + o.getIdCliente().getNome() : "";
                        setText(nomeObra(o) + cli);
                    }
                    return this;
                }
            });

            areaPreview = new JTextArea(4, 30);
            areaPreview.setEditable(false);
            areaPreview.setLineWrap(true);
            areaPreview.setWrapStyleWord(true);
            areaPreview.setOpaque(false);
            areaPreview.setFont(areaPreview.getFont().deriveFont(Font.BOLD, 13f));
            areaPreview.setForeground(UIConstants.COLOR_ADMIN_ACCENT);
            campoValor     = new JTextField();
            campoDescricao = new JTextField();

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));

            comboObra.addActionListener(e -> actualizarPreview());

            int row = 0;

            addFormRow(corpo, c, row++, "Obra com orçamento aprovado *", comboObra, 8);
            addFormRow(corpo, c, row++, "Total calculado a partir do orçamento:", areaPreview, 12);
            addFormRow(corpo, c, row++, "Valor a faturar *", campoValor, 12);
            addFormRow(corpo, c, row++, "Descrição / parcela", campoDescricao, 12);

            c.gridy = row * 2; c.insets = new Insets(4, 0, 0, 0);
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 0, 0, 0));

            JButton btnCancelar = buildButton("Cancelar");
            JButton btnEmitir   = buildButton("Emitir Fatura");
            btnEmitir.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnEmitir.setForeground(Color.WHITE);

            btnCancelar.addActionListener(e -> dialogo.dispose());
            btnEmitir.addActionListener(e -> {
                lblErro.setText(" ");
                try { valorEmitir(); } catch (Exception ex) {
                    lblErro.setText("Valor inválido."); return;
                }
                confirmado = true;
                dialogo.dispose();
            });

            rodape.add(btnCancelar);
            rodape.add(btnEmitir);
            add(rodape, BorderLayout.SOUTH);

            actualizarPreview();
        }

        private void addFormRow(JPanel corpo, GridBagConstraints c,
                                int row, String labelText, JComponent field, int topInset) {
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(topInset, 0, 2, 0);
            corpo.add(lbl, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(field, c);
        }

        private void actualizarPreview() {
            Obra obra = (Obra) comboObra.getSelectedItem();
            if (obra == null) { areaPreview.setText("—"); return; }

            orcamentoService.buscarAprovadoPorObra(obra).ifPresentOrElse(orc -> {
                BigDecimal totalOrcamento = orcamentoService.calcularTotais(orc).totalComIva();
                BigDecimal totalFaturado  = faturaService.totalFaturado(obra);
                BigDecimal saldo          = faturaService.saldoPorFaturar(obra, orc);
                areaPreview.setText("Orçamento aprovado: " + euros(totalOrcamento)
                        + "\nJá faturado: " + euros(totalFaturado)
                        + "\nPor faturar nesta obra: " + euros(saldo)
                        + "\nPrazo de pagamento previsto: " + prazoPagamentoTexto(orc));
                campoValor.setText(saldo.setScale(2, RoundingMode.HALF_UP).toPlainString());
                if (campoDescricao.getText().isBlank()) {
                    int parcela = faturaService.buscarPorObra(obra).size() + 1;
                    campoDescricao.setText("Parcela " + parcela + " do orçamento aprovado");
                }
            }, () -> areaPreview.setText("Sem orçamento aprovado."));
        }

        boolean     confirmado()   { return confirmado; }
        Obra        obraEscolhida(){ return (Obra) comboObra.getSelectedItem(); }
        BigDecimal  valorEmitir()  { return new BigDecimal(campoValor.getText().trim().replace(",", ".")); }
        String      descricao()    { return campoDescricao.getText().trim(); }
    }

    // =========================================================================
    //  FORMULÁRIO – Registar pagamento
    // =========================================================================

    private class PagamentoForm extends JPanel {

        private boolean confirmado = false;
        private final JTextField        campoValor;
        private final JTextField        campoData;
        private final JComboBox<String> comboMeioPagamento;
        private final JTextField        campoReferencia;
        private final JTextArea         campoObservacoes;
        private final JLabel            lblErro;

        PagamentoForm(JDialog dialogo, Fatura fatura, BigDecimal saldo) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(520, 500));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            campoValor = new JTextField();
            campoValor.setText(saldo.setScale(2, RoundingMode.HALF_UP).toPlainString());
            campoData  = new JTextField(LocalDate.now().format(FMT));
            campoData.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
            comboMeioPagamento = new JComboBox<>(new String[]{
                    "Transferência Bancária", "Multibanco", "MB Way", "Numerário", "Cheque", "Outro"
            });
            campoReferencia = new JTextField();
            campoReferencia.putClientProperty("JTextField.placeholderText", "Ex.: TRX-2026-00034");
            campoObservacoes = new JTextArea(4, 22);
            campoObservacoes.setLineWrap(true);
            campoObservacoes.setWrapStyleWord(true);
            campoObservacoes.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225)),
                    new EmptyBorder(8, 8, 8, 8)));

            // Resumo da fatura
            JLabel lblInfo = new JLabel(String.format(
                    "<html>%s — %s<br>Base: <b>%s</b> &nbsp;|&nbsp; IVA: <b>%s</b> &nbsp;|&nbsp; Total: <b>%s</b><br>" +
                            "Já pago: <b>%s</b> &nbsp;|&nbsp; Saldo em dívida: <b>%s</b><br>" +
                            "Vencimento: <b>%s</b></html>",
                    codigoOuFallback(fatura), nomeObra(fatura.getIdObra()),
                    euros(faturaService.valorSubtotalSemIva(fatura)),
                    euros(faturaService.valorIva(fatura)),
                    euros(fatura.getValorTotalComIva()),
                    euros(fatura.getValorPago()),
                    euros(saldo),
                    fatura.getDataVencimento() != null ? fatura.getDataVencimento().format(FMT) : "—"));
            JPanel cardInfo = new JPanel(new BorderLayout());
            cardInfo.setBackground(Color.WHITE);
            cardInfo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    new EmptyBorder(10, 12, 10, 12)));
            cardInfo.add(lblInfo, BorderLayout.CENTER);

            int row = 0;
            c.gridy = 0; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(0, 0, 12, 0);
            corpo.add(cardInfo, c);
            row++;

            addFormRow(corpo, c, row++, "Valor a registar (€) *",           campoValor,          4);
            addFormRow(corpo, c, row++, "Data do pagamento (dd/MM/yyyy) *",  campoData,           8);
            addFormRow(corpo, c, row++, "Meio de pagamento *",               comboMeioPagamento,  8);
            addFormRow(corpo, c, row++, "Referência / comprovativo",         campoReferencia,     8);
            addFormRow(corpo, c, row++, "Observações",
                    new JScrollPane(campoObservacoes), 8);

            // Ações rápidas
            JPanel acoesValor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            acoesValor.setOpaque(false);
            JButton btnTotal  = buildSmallButton("Total em dívida (" + euros(saldo) + ")");
            JButton btnLimpar = buildSmallButton("Limpar");
            btnTotal.addActionListener(e  -> campoValor.setText(saldo.toPlainString()));
            btnLimpar.addActionListener(e -> campoValor.setText(""));
            acoesValor.add(btnTotal);
            acoesValor.add(btnLimpar);
            c.gridy = row * 2; c.insets = new Insets(4, 0, 4, 0);
            corpo.add(acoesValor, c);
            row++;

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = row * 2; c.insets = new Insets(4, 0, 0, 0);
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 0, 0, 0));

            JButton btnCancelar  = buildButton("Cancelar");
            JButton btnConfirmar = buildButton("Confirmar");
            btnConfirmar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnConfirmar.setForeground(Color.WHITE);

            btnCancelar.addActionListener(e -> dialogo.dispose());
            btnConfirmar.addActionListener(e -> {
                if (validar(saldo)) { confirmado = true; dialogo.dispose(); }
            });

            rodape.add(btnCancelar);
            rodape.add(btnConfirmar);
            add(rodape, BorderLayout.SOUTH);
        }

        private void addFormRow(JPanel corpo, GridBagConstraints c,
                                int row, String labelText, JComponent field, int topInset) {
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(topInset, 0, 2, 0);
            corpo.add(lbl, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(field, c);
        }

        private boolean validar(BigDecimal saldo) {
            String txt = campoValor.getText().trim().replace(",", ".");
            try {
                BigDecimal v = new BigDecimal(txt);
                if (v.signum() <= 0) throw new NumberFormatException();
                if (v.compareTo(saldo) > 0) {
                    lblErro.setText(String.format("Máximo permitido: %.2f €", saldo));
                    return false;
                }
            } catch (NumberFormatException ex) {
                lblErro.setText("Introduz um valor numérico válido e maior que zero.");
                return false;
            }
            try { LocalDate.parse(campoData.getText().trim(), FMT); }
            catch (DateTimeParseException ex) {
                lblErro.setText("A data tem de estar no formato dd/MM/yyyy.");
                return false;
            }
            Object meio = comboMeioPagamento.getSelectedItem();
            if (meio == null || meio.toString().isBlank()) {
                lblErro.setText("Selecciona um meio de pagamento.");
                return false;
            }
            lblErro.setText(" ");
            return true;
        }

        boolean    confirmado()         { return confirmado; }
        BigDecimal valorPago()          { return new BigDecimal(campoValor.getText().trim().replace(",", ".")); }
        LocalDate  dataPagamento()      { return LocalDate.parse(campoData.getText().trim(), FMT); }
        String     meioPagamento()      { return comboMeioPagamento.getSelectedItem() != null ? comboMeioPagamento.getSelectedItem().toString() : null; }
        String     referenciaPagamento(){ return campoReferencia.getText().trim(); }
        String     observacoes()        { return campoObservacoes.getText().trim(); }
    }

    // =========================================================================
    //  PAINEL DETALHE — reorganizado
    // =========================================================================

    private class DetalhePanel extends JPanel {

        // Paleta de secções
        private static final Color COR_SECAO_BG     = new Color(248, 250, 252);
        private static final Color COR_SECAO_BORDA  = new Color(226, 232, 240);
        private static final Color COR_LABEL        = new Color(100, 116, 139);
        private static final Color COR_VALOR        = new Color(15,  23,  42);
        private static final Color COR_TITULO_TEXT  = new Color(51,  65,  85);

        // Cores de estado
        private static final Color COR_PAGO_BG      = new Color(220, 252, 231);
        private static final Color COR_PAGO_FG      = new Color(22,  101, 52);
        private static final Color COR_PARCIAL_BG   = new Color(254, 249, 195);
        private static final Color COR_PARCIAL_FG   = new Color(133, 77,  14);
        private static final Color COR_VENCIDA_BG   = new Color(254, 226, 226);
        private static final Color COR_VENCIDA_FG   = new Color(153, 27,  27);
        private static final Color COR_PENDENTE_BG  = new Color(255, 237, 213);
        private static final Color COR_PENDENTE_FG  = new Color(154, 52,  18);

        DetalhePanel(Fatura fatura) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(24, 28, 28, 28));
            setPreferredSize(new Dimension(800, 680));

            // 1. Cabeçalho do documento
            add(buildCabecalhoDocumento(fatura));
            add(vSpacer(16));

            // 2. Faixa de totais financeiros
            add(buildFaixaTotais(fatura));
            add(vSpacer(20));

            // 3. Secção: Cliente + Obra em colunas lado a lado
            add(buildSecaoClienteObra(fatura));
            add(vSpacer(16));

            // 4. Secção: Resumo fiscal (IVA)
            add(buildSecaoResumoIva(fatura));
            add(vSpacer(16));

            // 5. Secção: Histórico de pagamentos
            add(buildSecaoPagamentos(fatura));
            add(vSpacer(4));
        }

        // ── 1. Cabeçalho ──────────────────────────────────────────────────────

        private JPanel buildCabecalhoDocumento(Fatura fatura) {
            JPanel p = new JPanel(new BorderLayout(16, 0));
            p.setOpaque(false);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Esquerda: código + número interno
            JPanel esq = new JPanel();
            esq.setOpaque(false);
            esq.setLayout(new BoxLayout(esq, BoxLayout.Y_AXIS));

            JLabel lblCodigo = new JLabel(codigoOuFallback(fatura));
            lblCodigo.setFont(lblCodigo.getFont().deriveFont(Font.BOLD, 22f));
            lblCodigo.setForeground(COR_VALOR);

            String parcelaStr = "Parcela " + (fatura.getNumeroParcela() != null ? fatura.getNumeroParcela() : 1);
            String dataStr    = fatura.getDataEmissao() != null
                    ? "Emitida em " + fatura.getDataEmissao().format(FMT) : "";
            String orcStr     = fatura.getIdOrcamento() != null
                    ? " · Orçamento V" + fatura.getIdOrcamento().getVersao() : "";
            JLabel lblMeta = new JLabel(parcelaStr + " · " + dataStr + orcStr);
            lblMeta.setFont(lblMeta.getFont().deriveFont(13f));
            lblMeta.setForeground(COR_LABEL);

            esq.add(lblCodigo);
            esq.add(Box.createVerticalStrut(4));
            esq.add(lblMeta);

            // Direita: badge de estado
            String estado = faturaService.estadoApresentacao(fatura);
            JLabel lblEstado = new JLabel(estado);
            lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
            lblEstado.setOpaque(true);
            lblEstado.setBorder(new EmptyBorder(6, 14, 6, 14));
            lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
            aplicarCoresEstado(lblEstado, estado);

            JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            dir.setOpaque(false);
            dir.add(lblEstado);

            p.add(esq, BorderLayout.CENTER);
            p.add(dir, BorderLayout.EAST);

            // Separador inferior
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(p, BorderLayout.CENTER);
            wrapper.add(new JSeparator(), BorderLayout.SOUTH);
            ((JSeparator) wrapper.getComponent(1)).setForeground(COR_SECAO_BORDA);
            return wrapper;
        }

        // ── 2. Faixa de totais ─────────────────────────────────────────────────

        private JPanel buildFaixaTotais(Fatura fatura) {
            BigDecimal base   = faturaService.valorSubtotalSemIva(fatura);
            BigDecimal iva    = faturaService.valorIva(fatura);
            BigDecimal total  = fatura.getValorTotalComIva();
            BigDecimal pago   = fatura.getValorPago();
            BigDecimal saldo  = faturaService.saldoEmDivida(fatura);

            JPanel faixa = new JPanel(new GridLayout(1, 5, 1, 0));
            faixa.setAlignmentX(Component.LEFT_ALIGNMENT);
            faixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            faixa.add(buildCartaoValor("Base s/IVA",    euros(base),  false));
            faixa.add(buildCartaoValor("IVA",           euros(iva),   false));
            faixa.add(buildCartaoValor("Total c/IVA",   euros(total), true));
            faixa.add(buildCartaoValor("Valor Pago",    euros(pago),  false));
            faixa.add(buildCartaoSaldo("Saldo em Dívida", saldo));

            return faixa;
        }

        private JPanel buildCartaoValor(String titulo, String valor, boolean destaque) {
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(destaque ? new Color(239, 246, 255) : COR_SECAO_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(destaque ? new Color(147, 197, 253) : COR_SECAO_BORDA),
                    new EmptyBorder(10, 14, 10, 14)));

            JLabel lblT = new JLabel(titulo);
            lblT.setFont(lblT.getFont().deriveFont(11f));
            lblT.setForeground(COR_LABEL);

            JLabel lblV = new JLabel(valor);
            lblV.setFont(lblV.getFont().deriveFont(Font.BOLD, destaque ? 15f : 14f));
            lblV.setForeground(destaque ? new Color(29, 78, 216) : COR_VALOR);

            card.add(lblT, BorderLayout.NORTH);
            card.add(lblV, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildCartaoSaldo(String titulo, BigDecimal saldo) {
            boolean pago = saldo != null && saldo.compareTo(BigDecimal.ZERO) <= 0;
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(pago ? new Color(220, 252, 231) : new Color(254, 226, 226));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(pago ? new Color(134, 239, 172) : new Color(252, 165, 165)),
                    new EmptyBorder(10, 14, 10, 14)));

            JLabel lblT = new JLabel(titulo);
            lblT.setFont(lblT.getFont().deriveFont(11f));
            lblT.setForeground(pago ? new Color(22, 101, 52) : new Color(153, 27, 27));

            JLabel lblV = new JLabel(euros(saldo));
            lblV.setFont(lblV.getFont().deriveFont(Font.BOLD, 15f));
            lblV.setForeground(pago ? new Color(22, 101, 52) : new Color(153, 27, 27));

            card.add(lblT, BorderLayout.NORTH);
            card.add(lblV, BorderLayout.CENTER);
            return card;
        }

        // ── 3. Secção Cliente + Obra ───────────────────────────────────────────

        private JPanel buildSecaoClienteObra(Fatura fatura) {
            JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
            p.setOpaque(false);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            // Bloco cliente
            JPanel blocoCliente = buildBloco("Cliente");
            addBlocoLinha(blocoCliente, "Nome",    textoOuTraco(fatura.getClienteNome()));
            addBlocoLinha(blocoCliente, "NIF",     textoOuTraco(fatura.getClienteNif()));
            addBlocoLinha(blocoCliente, "Morada",  textoOuTraco(fatura.getClienteMorada()));

            // Bloco obra
            JPanel blocoObra = buildBloco("Obra");
            addBlocoLinha(blocoObra, "Descrição", nomeObra(fatura.getIdObra()));
            addBlocoLinha(blocoObra, "Morada",    textoOuTraco(fatura.getObraMorada()));
            addBlocoLinha(blocoObra, "Descrição fatura", textoOuTraco(fatura.getDescricao()));

            p.add(blocoCliente);
            p.add(blocoObra);
            return p;
        }

        // ── 4. Resumo IVA ──────────────────────────────────────────────────────

        private JPanel buildSecaoResumoIva(Fatura fatura) {
            JPanel secao = new JPanel(new BorderLayout(0, 8));
            secao.setOpaque(false);
            secao.setAlignmentX(Component.LEFT_ALIGNMENT);

            secao.add(buildTituloSecao("Resumo Fiscal"), BorderLayout.NORTH);

            DefaultTableModel modeloResumo = new DefaultTableModel(
                    new String[]{"Taxa IVA", "Base Tributável", "Valor IVA", "Total c/IVA"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            List<ResumoIva> resumoIva = faturaService.listarResumoIva(fatura);
            if (resumoIva.isEmpty()) {
                modeloResumo.addRow(new Object[]{"—", "—", "—", "—"});
            } else {
                for (ResumoIva r : resumoIva) {
                    modeloResumo.addRow(new Object[]{
                            r.taxaPercentagem().stripTrailingZeros().toPlainString() + "%",
                            euros(r.baseTributavel()),
                            euros(r.valorIva()),
                            euros(r.totalComIva())
                    });
                }
            }

            JTable t = buildTabelaDetalhe(modeloResumo);
            ajustarAlturaTabela(t);

            JScrollPane scroll = buildTablePane(t);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            secao.add(scroll, BorderLayout.CENTER);
            return secao;
        }

        // ── 5. Histórico de pagamentos ─────────────────────────────────────────

        private JPanel buildSecaoPagamentos(Fatura fatura) {
            JPanel secao = new JPanel(new BorderLayout(0, 8));
            secao.setOpaque(false);
            secao.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Cabeçalho da secção: título + info de vencimento
            JPanel cabecalhoPag = new JPanel(new BorderLayout());
            cabecalhoPag.setOpaque(false);
            cabecalhoPag.add(buildTituloSecao("Histórico de Pagamentos"), BorderLayout.WEST);

            if (fatura.getDataVencimento() != null) {
                boolean vencida = fatura.getDataVencimento().isBefore(LocalDate.now());
                String  textoVenc = (vencida ? "⚠ Vencida em " : "Vence em ")
                        + fatura.getDataVencimento().format(FMT);
                JLabel lblVenc = new JLabel(textoVenc);
                lblVenc.setFont(lblVenc.getFont().deriveFont(12f));
                lblVenc.setForeground(vencida ? new Color(153, 27, 27) : COR_LABEL);
                JPanel wrapVenc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
                wrapVenc.setOpaque(false);
                wrapVenc.add(lblVenc);
                cabecalhoPag.add(wrapVenc, BorderLayout.EAST);
            }

            if (fatura.getObservacoesPagamento() != null && !fatura.getObservacoesPagamento().isBlank()) {
                JLabel lblObsPag = new JLabel("Obs.: " + fatura.getObservacoesPagamento().trim());
                lblObsPag.setFont(lblObsPag.getFont().deriveFont(Font.ITALIC, 12f));
                lblObsPag.setForeground(COR_LABEL);
                cabecalhoPag.add(lblObsPag, BorderLayout.SOUTH);
            }

            secao.add(cabecalhoPag, BorderLayout.NORTH);

            DefaultTableModel modeloPag = new DefaultTableModel(
                    new String[]{"Data", "Meio de Pagamento", "Referência", "Valor Pago", "Observações"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            List<PagamentoFatura> pagamentos = faturaService.listarPagamentos(fatura);
            if (pagamentos.isEmpty()) {
                modeloPag.addRow(new Object[]{"—", "Sem pagamentos registados", "—", "—", "—"});
            } else {
                for (PagamentoFatura pag : pagamentos) {
                    modeloPag.addRow(new Object[]{
                            pag.getDataPagamento() != null ? pag.getDataPagamento().format(FMT) : "—",
                            textoOuTraco(pag.getMeioPagamento()),
                            textoOuTraco(pag.getReferenciaPagamento()),
                            euros(pag.getValorPago()),
                            textoOuTraco(pag.getObservacoes())
                    });
                }
            }

            JTable t = buildTabelaDetalhe(modeloPag);
            t.getColumnModel().getColumn(0).setPreferredWidth(90);
            t.getColumnModel().getColumn(1).setPreferredWidth(150);
            t.getColumnModel().getColumn(2).setPreferredWidth(130);
            t.getColumnModel().getColumn(3).setPreferredWidth(100);
            t.getColumnModel().getColumn(4).setPreferredWidth(200);
            ajustarAlturaTabela(t);

            JScrollPane scroll = buildTablePane(t);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            secao.add(scroll, BorderLayout.CENTER);
            return secao;
        }

        // ── Utilitários de UI internos ─────────────────────────────────────────

        /** Bloco tipo card com título e linhas label:valor */
        private JPanel buildBloco(String titulo) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(COR_SECAO_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COR_SECAO_BORDA),
                    new EmptyBorder(12, 14, 12, 14)));

            JLabel lblTitulo = new JLabel(titulo.toUpperCase());
            lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 10f));
            lblTitulo.setForeground(COR_LABEL);
            lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));
            lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lblTitulo);
            return card;
        }

        private void addBlocoLinha(JPanel bloco, String label, String valor) {
            JPanel linha = new JPanel(new BorderLayout(8, 0));
            linha.setOpaque(false);
            linha.setAlignmentX(Component.LEFT_ALIGNMENT);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            linha.setBorder(new EmptyBorder(2, 0, 2, 0));

            JLabel lblL = new JLabel(label);
            lblL.setFont(lblL.getFont().deriveFont(12f));
            lblL.setForeground(COR_LABEL);
            lblL.setPreferredSize(new Dimension(110, 18));

            JLabel lblV = new JLabel(valor);
            lblV.setFont(lblV.getFont().deriveFont(Font.BOLD, 12f));
            lblV.setForeground(COR_VALOR);

            linha.add(lblL, BorderLayout.WEST);
            linha.add(lblV, BorderLayout.CENTER);
            bloco.add(linha);
        }

        /** Título de secção com linha separadora */
        private JPanel buildTituloSecao(String titulo) {
            JPanel p = new JPanel(new BorderLayout(8, 0));
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(0, 0, 8, 0));

            JLabel lbl = new JLabel(titulo);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
            lbl.setForeground(COR_TITULO_TEXT);

            JSeparator sep = new JSeparator();
            sep.setForeground(COR_SECAO_BORDA);

            p.add(lbl, BorderLayout.WEST);
            p.add(sep, BorderLayout.CENTER);
            return p;
        }

        /** Tabela interna de detalhe com estilo consistente */
        private JTable buildTabelaDetalhe(DefaultTableModel tableModel) {
            JTable t = new JTable(tableModel);
            t.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
            t.setShowVerticalLines(false);
            t.getTableHeader().setReorderingAllowed(false);
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            t.setFillsViewportHeight(false);
            return t;
        }

        /** Ajusta a altura do ScrollPane para mostrar todas as linhas sem scroll */
        private void ajustarAlturaTabela(JTable t) {
            int altura = t.getRowCount() * t.getRowHeight()
                    + t.getTableHeader().getPreferredSize().height;
            t.setPreferredScrollableViewportSize(new Dimension(760, altura));
        }

        private void aplicarCoresEstado(JLabel lbl, String estado) {
            if ("Pago".equalsIgnoreCase(estado)) {
                lbl.setBackground(COR_PAGO_BG);    lbl.setForeground(COR_PAGO_FG);
            } else if ("Parcial".equalsIgnoreCase(estado)) {
                lbl.setBackground(COR_PARCIAL_BG); lbl.setForeground(COR_PARCIAL_FG);
            } else if ("Vencida".equalsIgnoreCase(estado)) {
                lbl.setBackground(COR_VENCIDA_BG); lbl.setForeground(COR_VENCIDA_FG);
            } else {
                lbl.setBackground(COR_PENDENTE_BG); lbl.setForeground(COR_PENDENTE_FG);
            }
        }

        private Component vSpacer(int altura) {
            return Box.createVerticalStrut(altura);
        }
    }
}