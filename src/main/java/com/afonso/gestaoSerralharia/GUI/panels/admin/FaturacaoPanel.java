package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
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
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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

    private static final String[] COLUNAS = {"ID", "Parcela", "Obra", "Cliente", "Descrição", "Data Emissão", "Total c/IVA", "Pago", "Saldo", "Estado"};
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

        List<Estadopagamento> estados = estadopagamentoService.listarTodos();
        String[] opcoes = new String[estados.size() + 1];
        opcoes[0] = "Todos";
        for (int i = 0; i < estados.size(); i++)
            opcoes[i + 1] = estados.get(i).getNomeEstado();

        filtroEstado = new JComboBox<>(opcoes);
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
        tabela.getColumnModel().getColumn(1).setPreferredWidth(65);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(9).setCellRenderer(new EstadoPagamentoRenderer());

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
            String estado = f.getIdEstadoPagamento() != null
                    ? f.getIdEstadoPagamento().getNomeEstado() : "—";

            if (filtroNomeEstado != null && !filtroNomeEstado.equals("Todos")
                    && !filtroNomeEstado.equals(estado)) continue;

            String obra    = f.getIdObra() != null ? nomeObra(f.getIdObra()) : "—";
            String cliente = f.getIdObra() != null && f.getIdObra().getIdCliente() != null
                    ? f.getIdObra().getIdCliente().getNome() : "—";
            String data    = f.getDataEmissao() != null ? f.getDataEmissao().format(FMT) : "—";
            String total   = euros(f.getValorTotalComIva());
            String pago    = euros(f.getValorPago());
            String saldo   = euros(f.getValorTotalComIva().subtract(f.getValorPago()));

            modelo.addRow(new Object[]{
                    f.getId(),
                    f.getNumeroParcela() != null ? f.getNumeroParcela() : 1,
                    obra,
                    cliente,
                    f.getDescricao() != null ? f.getDescricao() : "—",
                    data,
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

    // ── Emitir fatura ─────────────────────────────────────────────────────────

    private void abrirDialogoEmitir() {
        // Obras com orçamento aprovado e sem fatura ainda
        List<Obra> candidatas = new java.util.ArrayList<>(
                orcamentoService.buscarAprovados().stream()
                        .map(Orcamento::getIdObra)
                        .filter(o -> o != null && o.getId() != null)
                        .collect(java.util.stream.Collectors.toMap(
                                Obra::getId,
                                o -> o,
                                (a, b) -> a,
                                LinkedHashMap::new))
                        .values());

        if (candidatas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não existem obras com orçamento aprovado e sem fatura.\n" +
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

        BigDecimal saldo = fatura.getValorTotalComIva().subtract(fatura.getValorPago());
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
                faturaService.registarPagamento(id, form.valorPago());
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

        // Ir buscar as linhas do orçamento associado à obra
        List<Linhaorcamento> linhas = List.of();
        if (fatura.getIdOrcamento() != null) {
            linhas = linhaorcamentoService.buscarPorOrcamento(fatura.getIdOrcamento());
        } else if (fatura.getIdObra() != null) {
            linhas = orcamentoService.buscarAprovadoPorObra(fatura.getIdObra())
                    .map(linhaorcamentoService::buscarPorOrcamento)
                    .orElse(List.of());
        }

        JDialog dlg = criarDialogo("Detalhe da Fatura #" + fatura.getId());
        dlg.add(new DetalhePanel(fatura, linhas));
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
                } else {
                    lbl.setBackground(new Color(254, 226, 226));
                    lbl.setForeground(new Color(153, 27, 27));
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
        private final JLabel          lblPreview;
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

            lblPreview = new JLabel(" ");
            lblPreview.setFont(lblPreview.getFont().deriveFont(Font.BOLD, 13f));
            lblPreview.setForeground(UIConstants.COLOR_ADMIN_ACCENT);
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
            corpo.add(lblPreview, c);
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
            if (obra == null) { lblPreview.setText("—"); return; }

            orcamentoService.buscarAprovadoPorObra(obra).ifPresentOrElse(orc -> {
                BigDecimal total = faturaService.saldoPorFaturar(obra, orc);
                lblPreview.setText(String.format("Por faturar: %.2f €", total));
                if (campoValor.getText().isBlank()) {
                    campoValor.setText(total.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }, () -> lblPreview.setText("Sem orçamento"));
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
        private final JTextField campoValor;
        private final JLabel     lblErro;

        PagamentoForm(JDialog dialogo, Fatura fatura, BigDecimal saldo) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(360, 220));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            campoValor = new JTextField();

            int row = 0;

            // Resumo da fatura
            JLabel lblInfo = new JLabel(String.format(
                    "<html>Fatura <b>#%d</b> — %s<br>Total: <b>%s</b> &nbsp;|&nbsp; Já pago: <b>%s</b><br>" +
                            "Saldo em dívida: <b>%s</b></html>",
                    fatura.getId(),
                    nomeObra(fatura.getIdObra()),
                    euros(fatura.getValorTotalComIva()),
                    euros(fatura.getValorPago()),
                    euros(saldo)));
            lblInfo.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
            lblInfo.setBorder(new EmptyBorder(0, 0, 12, 0));
            c.gridy = 0; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(0, 0, 12, 0);
            corpo.add(lblInfo, c);
            row++;

            JLabel lblCampo = new JLabel("Valor a registar (€) *");
            lblCampo.setFont(lblCampo.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = row * 2; c.insets = new Insets(4, 0, 2, 0);
            corpo.add(lblCampo, c);
            c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
            corpo.add(campoValor, c);
            row++;

            // Botão "Pagar total"
            JButton btnTotal = buildSmallButton("Pagar total (" + euros(saldo) + ")");
            btnTotal.addActionListener(e -> campoValor.setText(saldo.toPlainString()));
            c.gridy = row * 2; c.insets = new Insets(4, 0, 4, 0);
            corpo.add(btnTotal, c);
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
            lblErro.setText(" ");
            return true;
        }

        boolean confirmado() { return confirmado; }

        BigDecimal valorPago() {
            return new BigDecimal(campoValor.getText().trim().replace(",", "."));
        }
    }

    // =========================================================================
    //  PAINEL DETALHE
    // =========================================================================

    private class DetalhePanel extends JPanel {

        DetalhePanel(Fatura fatura, List<Linhaorcamento> linhas) {
            setLayout(new BorderLayout(0, 16));
            setBorder(new EmptyBorder(24, 28, 24, 28));
            setPreferredSize(new Dimension(560, 420));

            // ── Cabeçalho info ────────────────────────────────────────────────
            JPanel info = new JPanel(new GridLayout(0, 2, 8, 4));
            info.setOpaque(false);

            String cliente = fatura.getIdObra() != null && fatura.getIdObra().getIdCliente() != null
                    ? fatura.getIdObra().getIdCliente().getNome() : "—";
            String email   = fatura.getIdObra() != null && fatura.getIdObra().getIdCliente() != null
                    ? (fatura.getIdObra().getIdCliente().getEmail() != null
                    ? fatura.getIdObra().getIdCliente().getEmail() : "—")
                    : "—";
            String data    = fatura.getDataEmissao() != null ? fatura.getDataEmissao().format(FMT) : "—";
            String estado  = fatura.getIdEstadoPagamento() != null
                    ? fatura.getIdEstadoPagamento().getNomeEstado() : "—";

            adicionarInfoLinha(info, "Fatura Nº",   "#" + fatura.getId());
            adicionarInfoLinha(info, "Parcela",      String.valueOf(fatura.getNumeroParcela() != null ? fatura.getNumeroParcela() : 1));
            adicionarInfoLinha(info, "Data emissão", data);
            adicionarInfoLinha(info, "Cliente",      cliente);
            adicionarInfoLinha(info, "Email",         email);
            adicionarInfoLinha(info, "Obra",          nomeObra(fatura.getIdObra()));
            adicionarInfoLinha(info, "Estado",        estado);
            adicionarInfoLinha(info, "Descrição",     fatura.getDescricao() != null ? fatura.getDescricao() : "—");
            adicionarInfoLinha(info, "Orçamento",     fatura.getIdOrcamento() != null ? "V" + fatura.getIdOrcamento().getVersao() : "—");

            add(info, BorderLayout.NORTH);

            // ── Tabela de linhas ──────────────────────────────────────────────
            String[] cols = {"Descrição", "Tipo", "Qtd", "Unit. s/IVA", "IVA %", "Subtotal c/IVA"};
            DefaultTableModel m = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            BigDecimal totalSIva = BigDecimal.ZERO;
            BigDecimal totalCIva = BigDecimal.ZERO;

            for (Linhaorcamento l : linhas) {
                BigDecimal sub = l.getPrecoUnit().multiply(l.getQuantidade());
                BigDecimal iva = l.getIvaPercentagemAplicada()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal subCIva = sub.multiply(BigDecimal.ONE.add(iva))
                        .setScale(2, RoundingMode.HALF_UP);
                totalSIva = totalSIva.add(sub);
                totalCIva = totalCIva.add(subCIva);

                String tipo = l.getIdTipoLinhaorcamento() != null
                        ? l.getIdTipoLinhaorcamento().getNomeTipo() : "—";
                m.addRow(new Object[]{
                        l.getNome() != null ? l.getNome() : "—",
                        tipo,
                        String.format("%.2f", l.getQuantidade()),
                        String.format("%.2f €", l.getPrecoUnit()),
                        String.format("%.0f%%", l.getIvaPercentagemAplicada()),
                        String.format("%.2f €", subCIva)
                });
            }

            JTable tbl = new JTable(m);
            tbl.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
            tbl.setShowVerticalLines(false);
            tbl.getTableHeader().setReorderingAllowed(false);
            tbl.setFillsViewportHeight(true);
            JScrollPane scroll = buildTablePane(tbl);

            add(scroll, BorderLayout.CENTER);

            // ── Totais ────────────────────────────────────────────────────────
            JPanel totais = new JPanel(new GridLayout(0, 2, 8, 4));
            totais.setOpaque(false);
            totais.setBorder(new EmptyBorder(12, 0, 0, 0));

            adicionarInfoLinha(totais, "Total s/IVA",    String.format("%.2f €", totalSIva));
            adicionarInfoLinha(totais, "Total c/IVA",    euros(fatura.getValorTotalComIva()));
            adicionarInfoLinha(totais, "Valor pago",     euros(fatura.getValorPago()));
            adicionarInfoLinha(totais, "Saldo em dívida",
                    euros(fatura.getValorTotalComIva().subtract(fatura.getValorPago())));

            add(totais, BorderLayout.SOUTH);
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
