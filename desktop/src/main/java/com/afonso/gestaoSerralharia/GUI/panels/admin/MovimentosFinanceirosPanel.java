package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Movimentofinanceiro;
import com.afonso.gestaoSerralharia.services.MovimentofinanceiroService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MovimentosFinanceirosPanel extends BasePanel {

    private final MovimentofinanceiroService movimentofinanceiroService;

    private JTextField filtroPesquisa;
    private JComboBox<String> filtroTipo;
    private JComboBox<String> filtroOrigem;
    private JTextField filtroDataInicio;
    private JTextField filtroDataFim;
    private DefaultTableModel modelo;
    private JTable tabela;
    private JLabel lblLucro;
    private JLabel lblGasto;
    private JLabel lblSaldo;
    private JLabel lblContador;

    private List<Movimentofinanceiro> movimentosCarregados = List.of();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUNAS = {"ID", "Data", "Tipo", "Origem", "Descrição", "Valor", "Impacto"};

    public MovimentosFinanceirosPanel(MovimentofinanceiroService movimentofinanceiroService) {
        this.movimentofinanceiroService = movimentofinanceiroService;

        add(buildHeader("Movimentos Monetários", "Histórico detalhado de ganhos e perdas"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        carregar();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildSurface(buildFiltros(), new Insets(10, 12, 10, 12)), BorderLayout.NORTH);
        body.add(buildTableArea(), BorderLayout.CENTER);
        body.add(buildSurface(buildRodape(), new Insets(10, 12, 10, 12)), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildFiltros() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        filtroPesquisa = buildSearchField("Pesquisar na descrição…");
        filtroPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
        });

        filtroTipo = new JComboBox<>(new String[]{"Todos os tipos", "GANHO", "PERDA", "OUTRO"});
        filtroTipo.setPreferredSize(new Dimension(170, 30));
        filtroTipo.addActionListener(e -> aplicarFiltros());

        filtroOrigem = new JComboBox<>(new String[]{"Todas as origens"});
        filtroOrigem.setPreferredSize(new Dimension(180, 30));
        filtroOrigem.addActionListener(e -> aplicarFiltros());

        filtroDataInicio = new JTextField();
        filtroDataInicio.putClientProperty("JTextField.placeholderText", "Início (dd/MM/yyyy)");
        filtroDataInicio.setPreferredSize(new Dimension(150, 30));
        filtroDataInicio.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
        });

        filtroDataFim = new JTextField();
        filtroDataFim.putClientProperty("JTextField.placeholderText", "Fim (dd/MM/yyyy)");
        filtroDataFim.setPreferredSize(new Dimension(150, 30));
        filtroDataFim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltros(); }
        });

        JButton btnLimpar = buildSmallButton("Limpar");
        btnLimpar.addActionListener(e -> {
            filtroPesquisa.setText("");
            filtroTipo.setSelectedIndex(0);
            filtroOrigem.setSelectedIndex(0);
            filtroDataInicio.setText("");
            filtroDataFim.setText("");
            aplicarFiltros();
        });

        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha.setOpaque(false);
        linha.add(filtroPesquisa);
        linha.add(filtroTipo);
        linha.add(filtroOrigem);
        linha.add(filtroDataInicio);
        linha.add(filtroDataFim);
        linha.add(btnLimpar);

        lblContador = new JLabel("0 movimentos");
        lblContador.setFont(lblContador.getFont().deriveFont(UIConstants.FONT_SMALL));
        lblContador.setForeground(UIManager.getColor("Label.disabledForeground"));

        panel.add(linha, BorderLayout.NORTH);
        panel.add(lblContador, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane buildTableArea() {
        modelo = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(340);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(90);

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) verDetalhe();
            }
        });

        return buildTablePane(tabela);
    }

    private JPanel buildRodape() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel totais = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        totais.setOpaque(false);
        lblLucro = statLabel("Lucro: 0,00 €", UIConstants.COLOR_SUCCESS);
        lblGasto = statLabel("Gasto: 0,00 €", UIConstants.COLOR_DANGER);
        lblSaldo = statLabel("Saldo: 0,00 €", UIConstants.COLOR_INFO);
        totais.add(lblLucro);
        totais.add(lblGasto);
        totais.add(lblSaldo);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        JButton btnDetalhe = buildSmallButton("Ver detalhe");
        JButton btnExportar = buildSmallButton("Exportar CSV");
        btnDetalhe.addActionListener(e -> verDetalhe());
        btnExportar.addActionListener(e -> exportarCsv());
        acoes.add(btnDetalhe);
        acoes.add(btnExportar);

        panel.add(totais, BorderLayout.WEST);
        panel.add(acoes, BorderLayout.EAST);
        return panel;
    }

    private JLabel statLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(color);
        return l;
    }

    private void carregar() {
        movimentosCarregados = new ArrayList<>(movimentofinanceiroService.listarTodos());
        movimentosCarregados.sort(Comparator.comparing(Movimentofinanceiro::getDataMovimento).reversed()
                .thenComparing(Movimentofinanceiro::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        popularOrigens();
        aplicarFiltros();
    }

    private void popularOrigens() {
        String atual = filtroOrigem != null && filtroOrigem.getSelectedItem() != null
                ? filtroOrigem.getSelectedItem().toString()
                : "Todas as origens";
        filtroOrigem.removeAllItems();
        filtroOrigem.addItem("Todas as origens");
        movimentosCarregados.stream()
                .map(Movimentofinanceiro::getOrigem)
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .sorted()
                .forEach(filtroOrigem::addItem);
        filtroOrigem.setSelectedItem(atual);
        if (filtroOrigem.getSelectedItem() == null) filtroOrigem.setSelectedIndex(0);
    }

    private void aplicarFiltros() {
        modelo.setRowCount(0);
        String q = filtroPesquisa != null ? filtroPesquisa.getText().trim().toLowerCase() : "";
        String tipo = filtroTipo != null ? (String) filtroTipo.getSelectedItem() : "Todos os tipos";
        String origem = filtroOrigem != null ? (String) filtroOrigem.getSelectedItem() : "Todas as origens";
        LocalDate dataInicio = parseData(filtroDataInicio != null ? filtroDataInicio.getText() : null);
        LocalDate dataFim = parseData(filtroDataFim != null ? filtroDataFim.getText() : null);

        BigDecimal lucro = BigDecimal.ZERO;
        BigDecimal gasto = BigDecimal.ZERO;
        int count = 0;

        for (Movimentofinanceiro m : movimentosCarregados) {
            if (!q.isBlank()) {
                String text = (m.getDescricao() != null ? m.getDescricao() : "").toLowerCase();
                if (!text.contains(q)) continue;
            }
            if (tipo != null && !"Todos os tipos".equals(tipo) && (m.getTipo() == null || !tipo.equalsIgnoreCase(m.getTipo()))) continue;
            if (origem != null && !"Todas as origens".equals(origem) && (m.getOrigem() == null || !origem.equalsIgnoreCase(m.getOrigem()))) continue;
            if (dataInicio != null && (m.getDataMovimento() == null || m.getDataMovimento().isBefore(dataInicio))) continue;
            if (dataFim != null && (m.getDataMovimento() == null || m.getDataMovimento().isAfter(dataFim))) continue;

            BigDecimal valor = m.getValor() != null ? m.getValor() : BigDecimal.ZERO;
            if (valor.signum() >= 0) lucro = lucro.add(valor);
            else gasto = gasto.add(valor.abs());

            modelo.addRow(new Object[]{
                    m.getId(),
                    m.getDataMovimento() != null ? m.getDataMovimento().format(FMT) : "—",
                    m.getTipo() != null ? m.getTipo() : "—",
                    m.getOrigem() != null ? m.getOrigem() : "—",
                    m.getDescricao() != null ? m.getDescricao() : "—",
                    euros(valor),
                    valor.signum() >= 0 ? "Ganho" : "Perda"
            });
            count++;
        }

        BigDecimal saldo = lucro.subtract(gasto);
        lblLucro.setText("Lucro: " + euros(lucro));
        lblGasto.setText("Gasto: " + euros(gasto));
        lblSaldo.setText("Saldo: " + euros(saldo));
        lblContador.setText(count + (count == 1 ? " movimento" : " movimentos"));
    }

    private void verDetalhe() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleciona um movimento primeiro.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Integer id = Integer.parseInt(modelo.getValueAt(tabela.convertRowIndexToModel(row), 0).toString());
        Movimentofinanceiro m = movimentosCarregados.stream().filter(x -> id.equals(x.getId())).findFirst().orElse(null);
        if (m == null) return;

        JPanel content = new JPanel(new GridLayout(0, 2, 8, 6));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        addDetail(content, "ID", String.valueOf(m.getId()));
        addDetail(content, "Data", m.getDataMovimento() != null ? m.getDataMovimento().format(FMT) : "—");
        addDetail(content, "Tipo", m.getTipo());
        addDetail(content, "Origem", m.getOrigem());
        addDetail(content, "Valor", euros(m.getValor()));
        addDetail(content, "Impacto", m.getValor() != null && m.getValor().signum() >= 0 ? "Ganho" : "Perda");
        addDetail(content, "Descrição", m.getDescricao());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setPreferredSize(new Dimension(520, 300));
        JOptionPane.showMessageDialog(this, scroll, "Detalhe do Movimento", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addDetail(JPanel p, String label, String value) {
        JLabel l1 = new JLabel(label + ":");
        l1.setFont(l1.getFont().deriveFont(Font.BOLD));
        JLabel l2 = new JLabel(value != null && !value.isBlank() ? value : "—");
        p.add(l1);
        p.add(l2);
    }

    private void exportarCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar movimentos para CSV");
        chooser.setSelectedFile(new File("movimentos_financeiros.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("id,data,tipo,origem,descricao,valor,impacto\n");
            for (int i = 0; i < modelo.getRowCount(); i++) {
                String id = csv(modelo.getValueAt(i, 0));
                String data = csv(modelo.getValueAt(i, 1));
                String tipo = csv(modelo.getValueAt(i, 2));
                String origem = csv(modelo.getValueAt(i, 3));
                String desc = csv(modelo.getValueAt(i, 4));
                String valor = csv(modelo.getValueAt(i, 5));
                String impacto = csv(modelo.getValueAt(i, 6));
                fw.write(String.join(",", id, data, tipo, origem, desc, valor, impacto));
                fw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "CSV exportado com sucesso:\n" + file.getAbsolutePath(), "Exportação", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar CSV: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(Object value) {
        String s = value != null ? value.toString() : "";
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private LocalDate parseData(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text.trim(), FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String euros(BigDecimal v) {
        BigDecimal n = v != null ? v : BigDecimal.ZERO;
        return n.setScale(2, RoundingMode.HALF_UP) + " €";
    }
}
