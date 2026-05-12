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

        JButton btnEmitir = buildButton("+ Emitir Fatura");
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
        panel.add(buildBarra(),       BorderLayout.NORTH);
        panel.add(buildAreaTabela(),  BorderLayout.CENTER);
        panel.add(buildRodape(),      BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBarra() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
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

        bar.add(new JLabel("Estado:"));
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
                    ? f.getIdObra().getIdCliente().getNome()
                    : "—");
            String data    = f.getDataEmissao() != null ? f.getDataEmissao().format(FMT) : "—";
            String vencimento = f.getDataVencimento() != null ? f.getDataVencimento().format(FMT) : "—";
            String base = euros(faturaService.valorSubtotalSemIva(f));
            String iva = euros(faturaService.valorIva(f));
            String total   = euros(f.getValorTotalComIva());
            String pago    = euros(f.getValorPago());
            String saldo   = euros(faturaService.saldoEmDivida(f));
            String documento = f.getCodigoDocumento() != null && !f.getCodigoDocumento().isBlank()
                    ? f.getCodigoDocumento()
                    : "FT-" + f.getId();

            modelo.addRow(new Object[]{
                    f.getId(),
                    documento,
                    f.getNumeroParcela() != null ? f.getNumeroParcela() : 1,
                    obra,
                    cliente,
                    data,
                    vencimento,
                    base,
                    iva,
                    total,
                    pago,
                    saldo,
                    estado
            });
        }
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

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
                ? fatura.getCodigoDocumento()
                : "FT-" + fatura.getId();
    }

    private static String prazoPagamentoTexto(Orcamento orcamento) {
        int prazo = orcamento != null && orcamento.getPrazoPagamentoDias() != null && orcamento.getPrazoPagamentoDias() > 0
                ? orcamento.getPrazoPagamentoDias()
                : 30;
        return prazo + " dias";
    }

    // ── Emitir fatura ─────────────────────────────────────────────────────────

    private void abrirDialogoEmitir() {
        // Obras com orçamento aprovado e saldo por faturar
        List<Obra> candidatas = new ArrayList<>(
                orcamentoService.buscarAprovados().stream()
                        .map(Orcamento::getIdObra)
                        .filter(o -> o != null && o.getId() != null)
                        .filter(o -> orcamentoService.buscarAprovadoPorObra(o)
                                .map(orc -> faturaService.saldoPorFaturar(o, orc).compareTo(BigDecimal.ZERO) > 0)
                                .orElse(false))
                        .collect(java.util.stream.Collectors.toMap(
                                Obra::getId,
                                o -> o,
                                (a, b) -> a,
                                LinkedHashMap::new))
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
        dlg.setLocationRelativeTo(this);
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
        dlg.setLocationRelativeTo(this);
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

        JDialog dlg = criarDialogo("Detalhe da Fatura #" + fatura.getId());
        dlg.add(new DetalhePanel(fatura));
        dlg.pack();
        dlg.setResizable(true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    private void eliminarSelecionada() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma fatura na tabela primeiro.",
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

    private static class EstadoPagamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
            setPreferredSize(new Dimension(460, 300));

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
            campoValor = new JTextField();
            campoDescricao = new JTextField();

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));

            // Actualizar preview ao mudar obra
            comboObra.addActionListener(e -> actualizarPreview());

            int row = 0;

            JLabel lbl1 = new JLabel("Obra com orçamento aprovado *");
            lbl1.setFont(lbl1.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(8, 0, 2, 0);
            corpo.add(lbl1, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(comboObra, c);
            row++;

            JLabel lbl2 = new JLabel("Total calculado a partir do orçamento:");
            lbl2.setFont(lbl2.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(12, 0, 2, 0);
            corpo.add(lbl2, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(areaPreview, c);
            row++;

            JLabel lbl3 = new JLabel("Valor a faturar *");
            lbl3.setFont(lbl3.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(12, 0, 2, 0);
            corpo.add(lbl3, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoValor, c);
            row++;

            JLabel lbl4 = new JLabel("Descrição / parcela");
            lbl4.setFont(lbl4.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(12, 0, 2, 0);
            corpo.add(lbl4, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoDescricao, c);
            row++;

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
                try {
                    valorEmitir();
                } catch (Exception ex) {
                    lblErro.setText("Valor inválido.");
                    return;
                }
                confirmado = true;
                dialogo.dispose();
            });

            rodape.add(btnCancelar);
            rodape.add(btnEmitir);
            add(rodape, BorderLayout.SOUTH);

            actualizarPreview();
        }

        private void actualizarPreview() {
            Obra obra = (Obra) comboObra.getSelectedItem();
            if (obra == null) { areaPreview.setText("—"); return; }

            orcamentoService.buscarAprovadoPorObra(obra).ifPresentOrElse(orc -> {
                BigDecimal totalOrcamento = orcamentoService.calcularTotais(orc).totalComIva();
                BigDecimal totalFaturado = faturaService.totalFaturado(obra);
                BigDecimal saldo = faturaService.saldoPorFaturar(obra, orc);
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

        boolean confirmado() { return confirmado; }
        Obra obraEscolhida() { return (Obra) comboObra.getSelectedItem(); }
        BigDecimal valorEmitir() { return new BigDecimal(campoValor.getText().trim().replace(",", ".")); }
        String descricao() { return campoDescricao.getText().trim(); }
    }

    // =========================================================================
    //  FORMULÁRIO – Registar pagamento
    // =========================================================================

    private class PagamentoForm extends JPanel {

        private boolean confirmado = false;
        private final JTextField      campoValor;
        private final JTextField      campoData;
        private final JComboBox<String> comboMeioPagamento;
        private final JTextField      campoReferencia;
        private final JTextArea       campoObservacoes;
        private final JLabel          lblErro;

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
            campoData = new JTextField(LocalDate.now().format(FMT));
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

            int row = 0;

            // Resumo da fatura
            JLabel lblInfo = new JLabel(String.format(
                    "<html>%s — %s<br>Base: <b>%s</b> &nbsp;|&nbsp; IVA: <b>%s</b> &nbsp;|&nbsp; Total: <b>%s</b><br>" +
                            "Já pago: <b>%s</b> &nbsp;|&nbsp; Saldo em dívida: <b>%s</b><br>" +
                            "Vencimento: <b>%s</b></html>",
                    codigoOuFallback(fatura),
                    nomeObra(fatura.getIdObra()),
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

            c.gridy = 0; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(0, 0, 12, 0);
            corpo.add(cardInfo, c);
            row++;

            JLabel lblCampo = new JLabel("Valor a registar (€) *");
            lblCampo.setFont(lblCampo.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(4, 0, 2, 0);
            corpo.add(lblCampo, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoValor, c);
            row++;

            JLabel lblData = new JLabel("Data do pagamento (dd/MM/yyyy) *");
            lblData.setFont(lblData.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(8, 0, 2, 0);
            corpo.add(lblData, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoData, c);
            row++;

            JLabel lblMeio = new JLabel("Meio de pagamento *");
            lblMeio.setFont(lblMeio.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(8, 0, 2, 0);
            corpo.add(lblMeio, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(comboMeioPagamento, c);
            row++;

            JLabel lblReferencia = new JLabel("Referência / comprovativo");
            lblReferencia.setFont(lblReferencia.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(8, 0, 2, 0);
            corpo.add(lblReferencia, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoReferencia, c);
            row++;

            JLabel lblObs = new JLabel("Observações");
            lblObs.setFont(lblObs.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(8, 0, 2, 0);
            corpo.add(lblObs, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(new JScrollPane(campoObservacoes), c);
            row++;

            // Ações rápidas de valor
            JPanel acoesValor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            acoesValor.setOpaque(false);
            JButton btnTotal = buildSmallButton("Total em dívida (" + euros(saldo) + ")");
            JButton btnLimpar = buildSmallButton("Limpar");
            btnTotal.addActionListener(e -> campoValor.setText(saldo.toPlainString()));
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
                if (validar(saldo)) {
                    confirmado = true;
                    dialogo.dispose();
                }
            });

            rodape.add(btnCancelar);
            rodape.add(btnConfirmar);
            add(rodape, BorderLayout.SOUTH);
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
            try {
                LocalDate.parse(campoData.getText().trim(), FMT);
            } catch (DateTimeParseException ex) {
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

        boolean confirmado() { return confirmado; }

        BigDecimal valorPago() {
            return new BigDecimal(campoValor.getText().trim().replace(",", "."));
        }

        LocalDate dataPagamento() {
            return LocalDate.parse(campoData.getText().trim(), FMT);
        }

        String meioPagamento() {
            return comboMeioPagamento.getSelectedItem() != null
                    ? comboMeioPagamento.getSelectedItem().toString()
                    : null;
        }

        String referenciaPagamento() {
            return campoReferencia.getText().trim();
        }

        String observacoes() {
            return campoObservacoes.getText().trim();
        }
    }

    // =========================================================================
    //  PAINEL DETALHE
    // =========================================================================

    private class DetalhePanel extends JPanel {

        DetalhePanel(Fatura fatura) {
            setLayout(new BorderLayout(0, 16));
            setBorder(new EmptyBorder(24, 28, 24, 28));
            setPreferredSize(new Dimension(760, 560));

            // ── Cabeçalho info ────────────────────────────────────────────────
            JPanel info = new JPanel(new GridLayout(0, 2, 8, 4));
            info.setOpaque(false);

            adicionarInfoLinha(info, "Documento", codigoOuFallback(fatura));
            adicionarInfoLinha(info, "Fatura Nº", "#" + fatura.getId());
            adicionarInfoLinha(info, "Parcela", String.valueOf(fatura.getNumeroParcela() != null ? fatura.getNumeroParcela() : 1));
            adicionarInfoLinha(info, "Data emissão", fatura.getDataEmissao() != null ? fatura.getDataEmissao().format(FMT) : "—");
            adicionarInfoLinha(info, "Vencimento", fatura.getDataVencimento() != null ? fatura.getDataVencimento().format(FMT) : "—");
            adicionarInfoLinha(info, "Estado", faturaService.estadoApresentacao(fatura));
            adicionarInfoLinha(info, "Cliente", textoOuTraco(fatura.getClienteNome()));
            adicionarInfoLinha(info, "NIF", textoOuTraco(fatura.getClienteNif()));
            adicionarInfoLinha(info, "Morada cliente", textoOuTraco(fatura.getClienteMorada()));
            adicionarInfoLinha(info, "Obra", nomeObra(fatura.getIdObra()));
            adicionarInfoLinha(info, "Morada obra", textoOuTraco(fatura.getObraMorada()));
            adicionarInfoLinha(info, "Descrição", textoOuTraco(fatura.getDescricao()));
            adicionarInfoLinha(info, "Orçamento", fatura.getIdOrcamento() != null ? "V" + fatura.getIdOrcamento().getVersao() : "—");

            add(info, BorderLayout.NORTH);

            add(buildCentroDetalhe(fatura), BorderLayout.CENTER);

            // ── Totais ────────────────────────────────────────────────────────
            JPanel totais = new JPanel(new GridLayout(0, 2, 8, 4));
            totais.setOpaque(false);
            totais.setBorder(new EmptyBorder(12, 0, 0, 0));

            adicionarInfoLinha(totais, "Base s/IVA", euros(faturaService.valorSubtotalSemIva(fatura)));
            adicionarInfoLinha(totais, "IVA", euros(faturaService.valorIva(fatura)));
            adicionarInfoLinha(totais, "Total c/IVA", euros(fatura.getValorTotalComIva()));
            adicionarInfoLinha(totais, "Valor pago", euros(fatura.getValorPago()));
            adicionarInfoLinha(totais, "Saldo em dívida", euros(faturaService.saldoEmDivida(fatura)));
            adicionarInfoLinha(totais, "Obs. pagamento", textoOuTraco(fatura.getObservacoesPagamento()));

            add(totais, BorderLayout.SOUTH);
        }

        private JComponent buildCentroDetalhe(Fatura fatura) {
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            content.add(secaoTitulo("Resumo fiscal"));
            content.add(buildResumoIva(fatura));
            content.add(Box.createVerticalStrut(14));
            content.add(secaoTitulo("Pagamentos"));
            content.add(buildPagamentos(fatura));
            return content;
        }

        private JComponent buildResumoIva(Fatura fatura) {
            DefaultTableModel modeloResumo = new DefaultTableModel(new String[]{"Taxa IVA", "Base", "IVA", "Total"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            List<ResumoIva> resumoIva = faturaService.listarResumoIva(fatura);
            if (resumoIva.isEmpty()) {
                modeloResumo.addRow(new Object[]{"—", "—", "—", "—"});
            } else {
                for (ResumoIva resumo : resumoIva) {
                    modeloResumo.addRow(new Object[]{
                            resumo.taxaPercentagem().stripTrailingZeros().toPlainString() + "%",
                            euros(resumo.baseTributavel()),
                            euros(resumo.valorIva()),
                            euros(resumo.totalComIva())
                    });
                }
            }

            JTable tabelaResumo = new JTable(modeloResumo);
            tabelaResumo.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
            tabelaResumo.setShowVerticalLines(false);
            tabelaResumo.getTableHeader().setReorderingAllowed(false);
            tabelaResumo.setFillsViewportHeight(true);
            JScrollPane scroll = buildTablePane(tabelaResumo);
            scroll.setPreferredSize(new Dimension(660, 160));
            return scroll;
        }

        private JComponent buildPagamentos(Fatura fatura) {
            DefaultTableModel modeloPagamentos = new DefaultTableModel(
                    new String[]{"Data", "Meio", "Referência", "Valor", "Observações"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            List<PagamentoFatura> pagamentos = faturaService.listarPagamentos(fatura);
            if (pagamentos.isEmpty()) {
                modeloPagamentos.addRow(new Object[]{"—", "—", "—", "—", "Sem pagamentos registados"});
            } else {
                for (PagamentoFatura pagamento : pagamentos) {
                    modeloPagamentos.addRow(new Object[]{
                            pagamento.getDataPagamento() != null ? pagamento.getDataPagamento().format(FMT) : "—",
                            textoOuTraco(pagamento.getMeioPagamento()),
                            textoOuTraco(pagamento.getReferenciaPagamento()),
                            euros(pagamento.getValorPago()),
                            textoOuTraco(pagamento.getObservacoes())
                    });
                }
            }

            JTable tabelaPagamentos = new JTable(modeloPagamentos);
            tabelaPagamentos.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
            tabelaPagamentos.setShowVerticalLines(false);
            tabelaPagamentos.getTableHeader().setReorderingAllowed(false);
            tabelaPagamentos.setFillsViewportHeight(true);
            JScrollPane scroll = buildTablePane(tabelaPagamentos);
            scroll.setPreferredSize(new Dimension(660, 200));
            return scroll;
        }

        private JComponent secaoTitulo(String titulo) {
            JLabel label = new JLabel(titulo);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
            label.setBorder(new EmptyBorder(0, 0, 6, 0));
            return label;
        }

        private void adicionarInfoLinha(JPanel panel, String label, String valor) {
            JLabel lLbl = new JLabel(label + ":");
            lLbl.setFont(lLbl.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
            JLabel lVal = new JLabel(valor);
            panel.add(lLbl);
            panel.add(lVal);
        }
    }
}
