package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.util.CalculosFinanceiros;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.util.TotaisFinanceiros;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrcamentosPanel extends BasePanel {

    private final OrcamentoService          orcamentoService;
    private final LinhaorcamentoService     linhaorcamentoService;
    private final ObraService               obraService;
    private final MaterialService           materialService;
    private final TaxaivaService            taxaivaService;
    private final TipolinhaorcamentoService tipoService;

    private static final String[] COLUNAS = {"ID", "Obra", "Cliente", "Versão", "Data", "Validade", "Base s/IVA", "Total c/IVA", "Estado", "Registo"};

    private DefaultTableModel modelo;
    private JTable            tabela;
    private JTextField        campoPesquisa;
    private JComboBox<String> filtroEstado;
    private JComboBox<String> filtroVersao;
    private JComboBox<String> filtroData;
    private JLabel            lblResumo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Dados empresa – carregados dinamicamente a partir do Dono autenticado

    // Cores PDF
    private static final DeviceRgb PDF_AZUL_ESC  = new DeviceRgb(30,  58,  95);
    private static final DeviceRgb PDF_AZUL_MED  = new DeviceRgb(52,  96, 152);
    private static final DeviceRgb PDF_CINZA_CLR = new DeviceRgb(230, 236, 245);
    private static final DeviceRgb PDF_LINHA_PAR = new DeviceRgb(248, 250, 253);
    private static final DeviceRgb PDF_SEP       = new DeviceRgb(200, 210, 225);

    public OrcamentosPanel(OrcamentoService orcamentoService,
                           LinhaorcamentoService linhaorcamentoService,
                           ObraService obraService,
                           MaterialService materialService,
                           TaxaivaService taxaivaService,
                           TipolinhaorcamentoService tipoService) {
        this.orcamentoService      = orcamentoService;
        this.linhaorcamentoService = linhaorcamentoService;
        this.obraService           = obraService;
        this.materialService       = materialService;
        this.taxaivaService        = taxaivaService;
        this.tipoService           = tipoService;

        JButton btnNovo = buildButton("Novo Orçamento");
        btnNovo.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFocusPainted(false);
        btnNovo.addActionListener(e -> abrirDialogoNovo());

        add(buildHeader("Orçamentos", "", btnNovo),
                BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);

        carregarTabela();

        SessionManager sM = SessionManager.getInstance();
    }

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildBarraFiltros(), BorderLayout.NORTH);
        panel.add(buildAreaTabela(),   BorderLayout.CENTER);
        panel.add(buildBarraAcoes(),   BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBarraFiltros() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
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

        filtroVersao = new JComboBox<>(new String[]{"Todas", "Só ativa", "Só histórico"});
        filtroVersao.setPreferredSize(new Dimension(130, UIConstants.SEARCH_FIELD_HEIGHT));
        filtroVersao.addActionListener(e -> carregarTabela());

        filtroData = new JComboBox<>(new String[]{"Qualquer data", "Últimos 30 dias", "Este ano"});
        filtroData.setPreferredSize(new Dimension(150, UIConstants.SEARCH_FIELD_HEIGHT));
        filtroData.addActionListener(e -> carregarTabela());

        JButton btnLimpar = buildSmallButton("Limpar");
        btnLimpar.addActionListener(e -> {
            campoPesquisa.setText("");
            filtroEstado.setSelectedIndex(0);
            filtroVersao.setSelectedIndex(0);
            filtroData.setSelectedIndex(0);
            carregarTabela();
        });

        lblResumo = new JLabel("0 orçamentos");
        lblResumo.setFont(lblResumo.getFont().deriveFont(Font.PLAIN, 12f));
        lblResumo.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setOpaque(false);
        esq.add(campoPesquisa);
        esq.add(filtroEstado);
        esq.add(filtroVersao);
        esq.add(filtroData);
        esq.add(btnLimpar);
        esq.add(lblResumo);

        bar.add(esq, BorderLayout.WEST);
        return bar;
    }

    private JScrollPane buildAreaTabela() {
        modelo = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
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
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(145);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(65);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(85);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(105);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(105);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(8).setCellRenderer(new EstadoOrcamentoRenderer());
        tabela.getColumnModel().getColumn(9).setCellRenderer(new RegistoRenderer());

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDialogoEditar();
            }
        });

        return buildTablePane(tabela);
    }

    private JPanel buildBarraAcoes() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setOpaque(false);

        JButton btnVer      = buildSmallButton("Ver / Editar");
        JButton btnAprovar  = buildSmallButton("Aprovar");
        JButton btnRevisao  = buildSmallButton("Criar revisão");
        JButton btnExportar = buildSmallButton("Exportar PDF");
        JButton btnEliminar = buildSmallButton("Eliminar");

        btnAprovar.setForeground(UIConstants.COLOR_SUCCESS);
        btnRevisao.setForeground(UIConstants.COLOR_WARNING);
        btnExportar.setForeground(UIConstants.COLOR_INFO);
        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnVer.addActionListener(e -> abrirDialogoEditar());
        btnAprovar.addActionListener(e -> aprovarSelecionado());
        btnRevisao.addActionListener(e -> criarRevisaoSelecionada());
        btnExportar.addActionListener(e -> exportarPDFSelecionado());
        btnEliminar.addActionListener(e -> eliminarSelecionado());

        bar.add(btnVer);
        bar.add(btnAprovar);
        bar.add(btnRevisao);
        bar.add(btnExportar);
        bar.add(btnEliminar);

        JLabel hint = new JLabel("  Duplo clique para abrir detalhes");
        hint.setFont(hint.getFont().deriveFont(UIConstants.FONT_SMALL));
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        bar.add(hint);
        return bar;
    }

    private void carregarTabela() {
        modelo.setRowCount(0);
        String filtro = campoPesquisa != null ? campoPesquisa.getText().trim().toLowerCase() : "";
        String estado = filtroEstado  != null ? (String) filtroEstado.getSelectedItem() : "Todos";
        String versao = filtroVersao  != null ? (String) filtroVersao.getSelectedItem() : "Todas";
        String dataSel = filtroData   != null ? (String) filtroData.getSelectedItem() : "Qualquer data";
        int total = 0;

        for (Orcamento o : orcamentoService.listarTodos()) {
            String nomeObra    = o.getIdObra() != null ? o.getIdObra().getDescricao() : "—";
            String nomeCliente = o.getIdObra() != null && o.getIdObra().getIdCliente() != null
                    ? o.getIdObra().getIdCliente().getNome() : "—";

            if (!filtro.isBlank() &&
                    !nomeObra.toLowerCase().contains(filtro) &&
                    !nomeCliente.toLowerCase().contains(filtro)) continue;

            boolean aprovado = Boolean.TRUE.equals(o.getAprovado());
            if ("Aprovado".equals(estado) && !aprovado) continue;
            if ("Pendente".equals(estado) &&  aprovado) continue;
            if ("Só ativa".equals(versao) && !Boolean.TRUE.equals(o.getAtivo())) continue;
            if ("Só histórico".equals(versao) && Boolean.TRUE.equals(o.getAtivo())) continue;
            if (!cumpreFiltroData(o, dataSel)) continue;

            TotaisFinanceiros totais = orcamentoService.calcularTotais(o);
            BigDecimal baseTributavel = baseTributavel(totais);
            LocalDate validade = orcamentoService.calcularDataValidade(o);
            modelo.addRow(new Object[]{
                    o.getId(), nomeObra, nomeCliente,
                    "V" + (o.getVersao() != null ? o.getVersao() : 1),
                    o.getDataEmissao() != null ? o.getDataEmissao().format(FMT) : "—",
                    validade != null ? validade.format(FMT) : "—",
                    baseTributavel.setScale(2, RoundingMode.HALF_UP) + " €",
                    totais.totalComIva().setScale(2, RoundingMode.HALF_UP) + " €",
                    aprovado ? "Aprovado" : "Pendente",
                    Boolean.TRUE.equals(o.getAtivo()) ? "Ativo" : "Histórico"
            });
            total++;
        }
        if (lblResumo != null) lblResumo.setText(total + (total == 1 ? " orçamento" : " orçamentos"));
    }

    private boolean cumpreFiltroData(Orcamento o, String dataSel) {
        if (o.getDataEmissao() == null || dataSel == null || "Qualquer data".equals(dataSel)) return true;
        LocalDate hoje = LocalDate.now();
        return switch (dataSel) {
            case "Últimos 30 dias" -> !o.getDataEmissao().isBefore(hoje.minusDays(30));
            case "Este ano" -> o.getDataEmissao().getYear() == hoje.getYear();
            default -> true;
        };
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    private void aprovarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        int op = JOptionPane.showConfirmDialog(this,
                "Aprovar este orçamento? A obra passará automaticamente para \"Em Execução\".",
                "Confirmar aprovação", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;
        try { orcamentoService.aprovar(id); carregarTabela();
            JOptionPane.showMessageDialog(this, "Orçamento aprovado.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void criarRevisaoSelecionada() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        try {
            Orcamento revisao = orcamentoService.criarRevisao(id);
            carregarTabela();
            JOptionPane.showMessageDialog(this,
                    "Foi criada a revisão V" + revisao.getVersao() + ".",
                    "Revisão criada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void eliminarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "Eliminar este orçamento? Todas as linhas serão removidas.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        try { orcamentoService.eliminar(id); carregarTabela(); }
        catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void exportarPDFSelecionado() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        try { gerarPDF(orcamentoService.buscarPorId(id)); }
        catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void abrirDialogoNovo() {
        List<Obra> obras = obraService.listarTodos();
        if (obras.isEmpty()) { aviso("Não existem obras registadas. Cria uma obra primeiro."); return; }
        JDialog dlg = criarDialogo("Novo Orçamento");
        FormOrcamento form = new FormOrcamento(dlg, null, obras);
        dlg.add(form); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
        if (form.confirmado()) {
            try { orcamentoService.guardar(form.construir()); carregarTabela(); }
            catch (Exception ex) { erro(ex.getMessage()); }
        }
    }

    private void abrirDialogoEditar() {
        Integer id = idSelecionado();
        if (id == null) { aviso("Seleciona um orçamento primeiro."); return; }
        Orcamento orc;
        try { orc = orcamentoService.buscarPorId(id); } catch (Exception ex) { erro(ex.getMessage()); return; }
        JDialog dlg = criarDialogo("Orçamento #" + orc.getId());
        DetalheOrcamento det = new DetalheOrcamento(dlg, orc);
        dlg.add(det); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
        carregarTabela();
    }

    // =========================================================================
    //  EXPORTAÇÃO PDF (iText 7)
    // =========================================================================

    private void gerarPDF(Orcamento orc) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Orçamento PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Ficheiros PDF (*.pdf)", "pdf"));
        String numDoc = "ORC-" + (orc.getDataEmissao() != null ? orc.getDataEmissao().getYear()
                : LocalDate.now().getYear()) + "-" + String.format("%04d", orc.getId());
        chooser.setSelectedFile(new File(numDoc + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".pdf")) dest = new File(dest.getAbsolutePath() + ".pdf");
        try {
            escreverPDF(orc, linhaorcamentoService.buscarPorOrcamento(orc), dest);
            JOptionPane.showMessageDialog(this, "PDF exportado:\n" + dest.getAbsolutePath(),
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { erro("Erro ao gerar PDF:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void escreverPDF(Orcamento orc, List<Linhaorcamento> linhas, File dest) throws Exception {
        // ── Dados da empresa a partir do Dono autenticado ──────────────────
        Dono dono = SessionManager.getInstance().getDono();
        String EMP_NOME   = empStr(dono != null ? dono.getEmpresaNome()      : null, "—");
        String EMP_MORADA = empStr(dono != null ? dono.getEmpresaMorada()    : null, "");
        String EMP_NIF    = "NIF: "  + empStr(dono != null ? dono.getEmpresaNif()       : null, "—");
        String EMP_TEL    = "Tel.: " + empStr(dono != null ? dono.getEmpresaTelefone()  : null, "—");
        String EMP_EMAIL  = empStr(dono != null ? dono.getEmpresaEmail()     : null, "—");
        String EMP_IBAN   = "IBAN: " + empStr(dono != null ? dono.getEmpresaIban()      : null, "—");

        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(dest));
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.setMargins(36, 45, 36, 45);

            Obra obra = orc.getIdObra();
            String cliente = obra != null && obra.getIdCliente() != null ? obra.getIdCliente().getNome() : "—";
            String nifCli = obra != null && obra.getIdCliente() != null && obra.getIdCliente().getNif() != null
                    ? obra.getIdCliente().getNif() : "—";
            String moradaObra = textoOuTraco(CalculosFinanceiros.formatarMoradaObra(obra));
            String moradaCli = obra != null && obra.getIdCliente() != null
                    ? textoOuTraco(formatarMoradaCli(obra.getIdCliente())) : "—";
            String data = orc.getDataEmissao() != null ? orc.getDataEmissao().format(FMT) : LocalDate.now().format(FMT);
            String numDoc = "ORC-" + (orc.getDataEmissao() != null ? orc.getDataEmissao().getYear()
                    : LocalDate.now().getYear()) + "-" + String.format("%04d", orc.getId());
            boolean aprov = Boolean.TRUE.equals(orc.getAprovado());
            LocalDate validade = orcamentoService.calcularDataValidade(orc);
            TotaisFinanceiros totaisDoc = CalculosFinanceiros.totaisLinhas(linhas);
            BigDecimal baseDoc = baseTributavel(totaisDoc);
            List<ResumoIva> resumoIva = orcamentoService.calcularResumoIva(orc);
            String observacoesFinanceiras = textoOuTraco(orc.getObservacoesFinanceiras());

            // ── 1. CABEÇALHO ────────────────────────────────────────────────
            Table hdr = new Table(new float[]{1f, 1f}).setWidth(UnitValue.createPercentValue(100));

            Cell cEmp = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(4);
            cEmp.add(pf(EMP_NOME,   bold,    13f, PDF_AZUL_ESC));
            cEmp.add(pf(EMP_MORADA, regular,  8f, ColorConstants.DARK_GRAY));
            cEmp.add(pf(EMP_NIF,    regular,  8f, ColorConstants.DARK_GRAY));
            cEmp.add(pf(EMP_TEL + "   |   " + EMP_EMAIL, regular, 8f, ColorConstants.DARK_GRAY));
            hdr.addCell(cEmp);

            Cell cDoc = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(4);
            cDoc.add(pf("ORÇAMENTO", bold, 20f, PDF_AZUL_ESC).setTextAlignment(TextAlignment.RIGHT));
            cDoc.add(pf(numDoc, bold, 10f, PDF_AZUL_MED).setTextAlignment(TextAlignment.RIGHT));
            cDoc.add(pf("Data de emissão: " + data, regular, 8f, ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.RIGHT));
            cDoc.add(pf("Válido até: " + (validade != null ? validade.format(FMT) : "—"),
                    regular, 8f, ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.RIGHT));
            cDoc.add(pf("Prazo de pagamento: " + prazoOuPadrao(orc.getPrazoPagamentoDias()) + " dias",
                    regular, 8f, ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.RIGHT));
            DeviceRgb corEstado = aprov ? new DeviceRgb(34, 139, 34) : new DeviceRgb(200, 100, 0);
            cDoc.add(pf("Estado: " + (aprov ? "APROVADO" : "PENDENTE"), bold, 9f, corEstado).setTextAlignment(TextAlignment.RIGHT));
            hdr.addCell(cDoc);
            doc.add(hdr);
            doc.add(new Paragraph().setBorderBottom(new SolidBorder(PDF_AZUL_ESC, 2)).setMarginBottom(10));

            // ── 2. CLIENTE / OBRA ───────────────────────────────────────────
            Table info = new Table(new float[]{1f, 1f}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(14);

            Cell cCli = blocoInfo("CLIENTE", bold);
            cCli.add(pf(cliente, bold, 10f, PDF_AZUL_ESC));
            cCli.add(pf("NIF: " + nifCli, regular, 8.5f, ColorConstants.DARK_GRAY));
            if (!moradaCli.isBlank()) cCli.add(pf(moradaCli, regular, 8.5f, ColorConstants.DARK_GRAY));
            info.addCell(cCli);

            Cell cObra = blocoInfo("LOCAL DA OBRA", bold);
            cObra.add(pf(obra != null && obra.getDescricao() != null ? obra.getDescricao() : "—", bold, 10f, PDF_AZUL_ESC));
            cObra.add(pf(moradaObra, regular, 8.5f, ColorConstants.DARK_GRAY));
            cObra.add(pf("Ref. Obra: #" + (obra != null ? obra.getId() : "—"), regular, 8f, ColorConstants.GRAY));
            info.addCell(cObra);
            doc.add(info);

            // ── 3. TABELA RESUMO PARA CLIENTE (agrupada por janela/grupo) ───────
            doc.add(pf("RESUMO DO ORÇAMENTO", bold, 9f, PDF_AZUL_ESC).setMarginBottom(4));

            // Agrupar linhas por grupo
            LinkedHashMap<String, java.util.List<Linhaorcamento>> porGrupo = new LinkedHashMap<>();
            for (Linhaorcamento l : linhas) {
                String nomeCompleto = l.getNome() != null ? l.getNome() : "";
                String grupo;
                if (nomeCompleto.startsWith("[")) {
                    int fim = nomeCompleto.indexOf(']');
                    grupo = fim > 1 ? nomeCompleto.substring(1, fim).trim() : nomeCompleto;
                } else {
                    grupo = nomeCompleto; // linha sem grupo: aparece com o próprio nome
                }
                porGrupo.computeIfAbsent(grupo, k -> new java.util.ArrayList<>()).add(l);
            }

            float[] colWRes = {25, 210, 70, 60, 70, 80};
            Table tRes = new Table(UnitValue.createPointArray(colWRes)).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(6);
            for (String cab : new String[]{"Nº", "Descrição", "Bruto", "Desc.", "Base s/IVA", "Total c/ IVA"}) {
                tRes.addHeaderCell(new Cell()
                        .add(new Paragraph(cab).setFont(bold).setFontSize(7.5f).setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(PDF_AZUL_ESC).setFontColor(ColorConstants.WHITE)
                        .setPadding(5).setBorder(Border.NO_BORDER));
            }

            int idxGrupo = 0;
            for (Map.Entry<String, java.util.List<Linhaorcamento>> entry : porGrupo.entrySet()) {
                idxGrupo++;
                DeviceRgb bg = (idxGrupo % 2 == 0) ? PDF_LINHA_PAR : null;
                TotaisFinanceiros grpTotais = CalculosFinanceiros.totaisLinhas(entry.getValue());
                BigDecimal grpBruto = grpTotais.subtotalSemIva();
                BigDecimal grpBase = baseTributavel(grpTotais);
                tRes.addCell(cL(String.valueOf(idxGrupo), regular, 8f, bg, TextAlignment.CENTER));
                tRes.addCell(cL(entry.getKey(), bold, 8f, bg, TextAlignment.LEFT));
                tRes.addCell(cL(fe(grpBruto), regular, 8f, bg, TextAlignment.RIGHT));
                tRes.addCell(cL(fe(grpTotais.valorDesconto()), regular, 8f, bg, TextAlignment.RIGHT));
                tRes.addCell(cL(fe(grpBase), regular, 8f, bg, TextAlignment.RIGHT));
                tRes.addCell(cL(fe(grpTotais.totalComIva()), bold, 8f, bg, TextAlignment.RIGHT));
            }
            if (linhas.isEmpty())
                tRes.addCell(new Cell(1, 6)
                        .add(new Paragraph("(sem linhas de orçamento)").setFont(regular).setFontSize(8)
                                .setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY))
                        .setBorder(Border.NO_BORDER).setPadding(8));
            doc.add(tRes);

            if (!resumoIva.isEmpty()) {
                doc.add(pf("RESUMO FISCAL", bold, 9f, PDF_AZUL_ESC).setMarginTop(4).setMarginBottom(4));
                Table tIva = new Table(UnitValue.createPointArray(new float[]{80, 90, 90, 90}))
                        .setWidth(UnitValue.createPercentValue(60))
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        .setMarginBottom(10);
                for (String cab : new String[]{"Taxa", "Base", "IVA", "Total"}) {
                    tIva.addHeaderCell(new Cell()
                            .add(new Paragraph(cab).setFont(bold).setFontSize(7.5f).setTextAlignment(TextAlignment.CENTER))
                            .setBackgroundColor(PDF_AZUL_MED).setFontColor(ColorConstants.WHITE)
                            .setPadding(5).setBorder(Border.NO_BORDER));
                }
                for (ResumoIva resumo : resumoIva) {
                    tIva.addCell(cL(resumo.taxaPercentagem().stripTrailingZeros().toPlainString() + "%", regular, 8f, null, TextAlignment.CENTER));
                    tIva.addCell(cL(fe(resumo.baseTributavel()), regular, 8f, null, TextAlignment.RIGHT));
                    tIva.addCell(cL(fe(resumo.valorIva()), regular, 8f, null, TextAlignment.RIGHT));
                    tIva.addCell(cL(fe(resumo.totalComIva()), bold, 8f, null, TextAlignment.RIGHT));
                }
                doc.add(tIva);
            }

            // ── 4. TOTAIS ───────────────────────────────────────────────────
            Table tOuter = new Table(new float[]{1f, 220f})
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT).setMarginBottom(18);
            tOuter.addCell(new Cell().setBorder(Border.NO_BORDER));

            Table tInner = new Table(new float[]{130f, 90f}).setWidth(220f);
            linhaTotal(tInner, "Bruto s/ IVA:", fe(totaisDoc.subtotalSemIva()), regular, bold, false);
            linhaTotal(tInner, "Desconto:", fe(totaisDoc.valorDesconto()), regular, bold, false);
            linhaTotal(tInner, "Base tributável:", fe(baseDoc), regular, bold, false);
            linhaTotal(tInner, "Total de IVA:", fe(totaisDoc.valorIva()), regular, bold, false);
            tInner.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER)
                    .setBorderTop(new SolidBorder(PDF_AZUL_ESC, 1.5f)).setPaddingTop(3).setPaddingBottom(3));
            linhaTotalDestaque(tInner, "TOTAL c/ IVA:", fe(totaisDoc.totalComIva()), bold);

            tOuter.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(tInner));
            doc.add(tOuter);

            // ── 5. CONDIÇÕES ────────────────────────────────────────────────
            doc.add(new Paragraph().setBorderBottom(new SolidBorder(PDF_SEP, 0.5f)).setMarginBottom(6));
            Table cond = new Table(new float[]{1f, 1f}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);
            Cell cPag = new Cell().setBorder(Border.NO_BORDER);
            cPag.add(pf("CONDIÇÕES DE PAGAMENTO", bold, 7f, PDF_AZUL_MED).setMarginBottom(3));
            cPag.add(pf("• Prazo de validade do orçamento: " + prazoOuPadrao(orc.getPrazoValidadeDias()) + " dias",
                    regular, 7.5f, ColorConstants.DARK_GRAY));
            cPag.add(pf("• Prazo de pagamento da fatura: " + prazoOuPadrao(orc.getPrazoPagamentoDias()) + " dias após emissão",
                    regular, 7.5f, ColorConstants.DARK_GRAY));
            cPag.add(pf("• Meios de pagamento habituais: transferência, multibanco ou numerário",
                    regular, 7.5f, ColorConstants.DARK_GRAY));
            cPag.add(pf(EMP_IBAN, regular, 7.5f, ColorConstants.DARK_GRAY));
            cond.addCell(cPag);
            Cell cNot = new Cell().setBorder(Border.NO_BORDER);
            cNot.add(pf("NOTAS", bold, 7f, PDF_AZUL_MED).setMarginBottom(3));
            cNot.add(pf("• Valores discriminados por base tributável, desconto e IVA aplicável.", regular, 7.5f, ColorConstants.DARK_GRAY));
            cNot.add(pf("• Trabalhos adicionais serão orçamentados separadamente.", regular, 7.5f, ColorConstants.DARK_GRAY));
            cNot.add(pf(observacoesFinanceiras.equals("—")
                            ? "• Sem observações financeiras adicionais."
                            : "• " + observacoesFinanceiras,
                    regular, 7.5f, ColorConstants.DARK_GRAY));
            cond.addCell(cNot);
            doc.add(cond);

            // ── 6. RODAPÉ ───────────────────────────────────────────────────
            doc.add(new Paragraph().setBorderTop(new SolidBorder(PDF_SEP, 0.5f)).setMarginTop(4));
            doc.add(pf(EMP_NOME + "  |  " + EMP_NIF + "  |  " + EMP_EMAIL, regular, 7f, ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(pf("Documento gerado em " + LocalDate.now().format(FMT) + "  –  " + numDoc,
                    regular, 7f, ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
        }
    }

    // Helpers PDF
    /** Devolve {@code val} se não for nulo/vazio, caso contrário devolve {@code fallback}. */
    private static String empStr(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private static Paragraph pf(String t, PdfFont f, float s, com.itextpdf.kernel.colors.Color c) {
        return new Paragraph(t).setFont(f).setFontSize(s).setFontColor(c).setMarginBottom(1);
    }
    private static Cell blocoInfo(String titulo, PdfFont bold) {
        Cell c = new Cell().setBackgroundColor(PDF_CINZA_CLR).setBorder(new SolidBorder(PDF_SEP, 0.5f)).setPadding(8);
        c.add(pf(titulo, bold, 7f, PDF_AZUL_MED).setMarginBottom(4));
        return c;
    }
    private static Cell cL(String t, PdfFont f, float s, DeviceRgb bg, TextAlignment a) {
        Cell c = new Cell().add(new Paragraph(t).setFont(f).setFontSize(s).setTextAlignment(a))
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(4).setPaddingRight(4)
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(PDF_SEP, 0.3f));
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }
    private static void linhaTotal(Table t, String lbl, String val, PdfFont fl, PdfFont fv, boolean d) {
        float s = d ? 10f : 8.5f;
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingTop(2).setPaddingBottom(2)
                .add(new Paragraph(lbl).setFont(fl).setFontSize(s).setFontColor(ColorConstants.DARK_GRAY)));
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingTop(2).setPaddingBottom(2)
                .add(new Paragraph(val).setFont(fv).setFontSize(s).setFontColor(ColorConstants.DARK_GRAY)
                        .setTextAlignment(TextAlignment.RIGHT)));
    }
    private static void linhaTotalDestaque(Table t, String lbl, String val, PdfFont fb) {
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingTop(2).setPaddingBottom(2)
                .add(new Paragraph(lbl).setFont(fb).setFontSize(10f).setFontColor(PDF_AZUL_ESC)));
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingTop(2).setPaddingBottom(2)
                .add(new Paragraph(val).setFont(fb).setFontSize(10f).setFontColor(PDF_AZUL_ESC)
                        .setTextAlignment(TextAlignment.RIGHT)));
    }
    private static String fe(BigDecimal v) {
        if (v == null) return "0,00 €";
        return String.format("%,.2f €", v).replace(",", "X").replace(".", ",").replace("X", ".");
    }
    private static String formatarMoradaCli(Cliente cli) {
        StringBuilder sb = new StringBuilder();
        if (cli.getRua()    != null) sb.append(cli.getRua());
        if (cli.getNporta() != null) sb.append(", nº ").append(cli.getNporta());
        if (cli.getIdCodpostal() != null) sb.append(" – ").append(cli.getIdCodpostal().getCodpostal());
        return sb.toString();
    }

    private static BigDecimal baseTributavel(TotaisFinanceiros totais) {
        if (totais == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return CalculosFinanceiros.normalizarDinheiro(totais.subtotalSemIva().subtract(totais.valorDesconto()));
    }

    private static int prazoOuPadrao(Integer prazoDias) {
        return prazoDias != null && prazoDias > 0 ? prazoDias : 30;
    }

    private static String textoOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor.trim();
    }

    // Utilitários Swing
    private JDialog criarDialogo(String titulo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, titulo, true)
                : new JDialog((Dialog) owner, titulo, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);
        return dlg;
    }
    private void aviso(String msg) { JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE); }
    private void erro(String msg)  { JOptionPane.showMessageDialog(this, msg, "Erro",  JOptionPane.ERROR_MESSAGE); }

    private static class EstadoOrcamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = value != null ? value.toString() : "";
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                if ("Aprovado".equalsIgnoreCase(estado)) {
                    lbl.setBackground(new Color(220, 252, 231));
                    lbl.setForeground(new Color(22, 101, 52));
                } else {
                    lbl.setBackground(new Color(255, 237, 213));
                    lbl.setForeground(new Color(154, 52, 18));
                }
            }
            return lbl;
        }
    }

    private static class RegistoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String registo = value != null ? value.toString() : "";
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                if ("Ativo".equalsIgnoreCase(registo)) {
                    lbl.setBackground(new Color(219, 234, 254));
                    lbl.setForeground(new Color(30, 64, 175));
                } else {
                    lbl.setBackground(new Color(241, 245, 249));
                    lbl.setForeground(new Color(71, 85, 105));
                }
            }
            return lbl;
        }
    }

    // =========================================================================
    //  FORMULÁRIO NOVO ORÇAMENTO
    // =========================================================================
    private class FormOrcamento extends JPanel {
        private boolean confirmado = false;
        private final Orcamento      orcExistente;
        private final JComboBox<Obra> comboObra;
        private final JTextField      campoPrazoValidade;
        private final JTextField      campoPrazoPagamento;
        private final JTextArea       campoObservacoes;
        private final JLabel          lblErro;

        FormOrcamento(JDialog dialogo, Orcamento orc, List<Obra> obras) {
            this.orcExistente = orc;
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(460, 350));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.insets = new Insets(4, 0, 4, 0);

            comboObra = new JComboBox<>(obras.toArray(new Obra[0]));
            comboObra.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(list, v, i, s, f);
                    if (v instanceof Obra o) {
                        String cli = o.getIdCliente() != null ? o.getIdCliente().getNome() : "—";
                        setText(o.getDescricao() + "  (" + cli + ")");
                    }
                    return this;
                }
            });
            campoPrazoValidade = new JTextField();
            campoPrazoPagamento = new JTextField();
            campoObservacoes = new JTextArea(4, 24);
            campoObservacoes.setLineWrap(true);
            campoObservacoes.setWrapStyleWord(true);

            int row = 0;
            addField(corpo, c, row++, "Obra *", comboObra);
            addField(corpo, c, row++, "Prazo de validade (dias) *", campoPrazoValidade);
            addField(corpo, c, row++, "Prazo de pagamento da futura fatura (dias) *", campoPrazoPagamento);
            addField(corpo, c, row++, "Observações financeiras", new JScrollPane(campoObservacoes));

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = row * 2;
            c.insets = new Insets(8, 0, 0, 0);
            corpo.add(lblErro, c);
            add(corpo, BorderLayout.CENTER);

            if (orc != null) {
                comboObra.setSelectedItem(orc.getIdObra());
                comboObra.setEnabled(false);
                campoPrazoValidade.setText(String.valueOf(prazoOuPadrao(orc.getPrazoValidadeDias())));
                campoPrazoPagamento.setText(String.valueOf(prazoOuPadrao(orc.getPrazoPagamentoDias())));
                campoObservacoes.setText(orc.getObservacoesFinanceiras() != null ? orc.getObservacoesFinanceiras() : "");
            } else {
                campoPrazoValidade.setText("30");
                campoPrazoPagamento.setText("30");
            }

            JPanel rod = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rod.setOpaque(false); rod.setBorder(new EmptyBorder(12, 0, 0, 0));
            JButton btnC = buildButton("Cancelar");
            JButton btnG = buildButton(orc == null ? "Criar Orçamento" : "Guardar Condições");
            btnG.setBackground(UIConstants.COLOR_ADMIN_ACCENT); btnG.setForeground(Color.WHITE);
            btnC.addActionListener(e -> dialogo.dispose());
            btnG.addActionListener(e -> {
                if (validar()) {
                    confirmado = true;
                    dialogo.dispose();
                }
            });
            rod.add(btnC); rod.add(btnG);
            add(rod, BorderLayout.SOUTH);
        }

        private void addField(JPanel panel, GridBagConstraints c, int index, String label, JComponent campo) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy = index * 2;
            c.insets = new Insets(index == 0 ? 0 : 10, 0, 2, 0);
            panel.add(lbl, c);
            c.gridy = index * 2 + 1;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(campo, c);
        }

        private boolean validar() {
            if (comboObra.getSelectedItem() == null) {
                lblErro.setText("A obra é obrigatória.");
                return false;
            }
            try {
                if (Integer.parseInt(campoPrazoValidade.getText().trim()) <= 0) {
                    lblErro.setText("O prazo de validade tem de ser maior que zero.");
                    return false;
                }
            } catch (NumberFormatException e) {
                lblErro.setText("Prazo de validade inválido.");
                return false;
            }
            try {
                if (Integer.parseInt(campoPrazoPagamento.getText().trim()) <= 0) {
                    lblErro.setText("O prazo de pagamento tem de ser maior que zero.");
                    return false;
                }
            } catch (NumberFormatException e) {
                lblErro.setText("Prazo de pagamento inválido.");
                return false;
            }
            lblErro.setText(" ");
            return true;
        }

        boolean confirmado() { return confirmado; }
        Orcamento construir() {
            Orcamento o = orcExistente != null ? orcExistente : new Orcamento();
            o.setIdObra((Obra) comboObra.getSelectedItem());
            o.setPrazoValidadeDias(Integer.parseInt(campoPrazoValidade.getText().trim()));
            o.setPrazoPagamentoDias(Integer.parseInt(campoPrazoPagamento.getText().trim()));
            o.setObservacoesFinanceiras(campoObservacoes.getText().trim().isBlank()
                    ? null
                    : campoObservacoes.getText().trim());
            if (orcExistente == null) {
                o.setDataEmissao(LocalDate.now());
                o.setAprovado(false);
                o.setAtivo(true);
                o.setVersao(1);
            }
            return o;
        }
    }

    // =========================================================================
    //  DETALHE ORÇAMENTO
    // =========================================================================
    private class DetalheOrcamento extends JPanel {
        private final JDialog   dialogo;
        private final Orcamento orc;
        private DefaultTableModel modeloLinhas;
        private JTable            tabelaLinhas;
        private static final String[] COL_LINHAS = {
                "ID", "Grupo/Janela", "Descrição", "Tipo", "IVA %", "Qtd", "P. Unit.", "Desc. %", "Base s/IVA", "Total c/IVA"
        };
        private JLabel lblTotal;

        DetalheOrcamento(JDialog dialogo, Orcamento orc) {
            this.dialogo = dialogo; this.orc = orc;
            setLayout(new BorderLayout()); setBorder(new EmptyBorder(20, 24, 16, 24));
            setPreferredSize(new Dimension(820, 560));
            add(buildCabecalho(),  BorderLayout.NORTH);
            add(buildAreaLinhas(), BorderLayout.CENTER);
            add(buildRodape(),     BorderLayout.SOUTH);
            carregarLinhas();
        }

        private JPanel buildCabecalho() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setOpaque(false); panel.setBorder(new EmptyBorder(0, 0, 12, 0));
            JPanel topo = new JPanel(new BorderLayout()); topo.setOpaque(false);
            String nomeObra    = orc.getIdObra() != null ? orc.getIdObra().getDescricao() : "—";
            String nomeCliente = orc.getIdObra() != null && orc.getIdObra().getIdCliente() != null ? orc.getIdObra().getIdCliente().getNome() : "—";
            String data = orc.getDataEmissao() != null ? orc.getDataEmissao().format(FMT) : "—";
            LocalDate validade = orcamentoService.calcularDataValidade(orc);
            JLabel lblO = new JLabel(nomeObra); lblO.setFont(lblO.getFont().deriveFont(Font.BOLD, 15f));
            topo.add(lblO, BorderLayout.WEST);
            boolean aprov = Boolean.TRUE.equals(orc.getAprovado());
            JLabel badge = new JLabel(aprov ? "  Aprovado  " : "  Pendente  ");
            badge.setOpaque(true); badge.setBackground(aprov ? UIConstants.COLOR_SUCCESS : UIConstants.COLOR_WARNING);
            badge.setForeground(Color.WHITE); badge.setFont(badge.getFont().deriveFont(Font.BOLD, UIConstants.FONT_SMALL));
            badge.setBorder(new EmptyBorder(3, 8, 3, 8)); topo.add(badge, BorderLayout.EAST);
            panel.add(topo, BorderLayout.NORTH);
            JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); meta.setOpaque(false);
            meta.add(metaLabel("Cliente", nomeCliente)); meta.add(metaLabel("Data emissão", data));
            meta.add(metaLabel("Orçamento nº", String.valueOf(orc.getId())));
            meta.add(metaLabel("Versão", "V" + (orc.getVersao() != null ? orc.getVersao() : 1)));
            meta.add(metaLabel("Validade", validade != null ? validade.format(FMT) : "—"));
            meta.add(metaLabel("Prazo pagamento", prazoOuPadrao(orc.getPrazoPagamentoDias()) + " dias"));
            panel.add(meta, BorderLayout.CENTER);

            if (orc.getObservacoesFinanceiras() != null && !orc.getObservacoesFinanceiras().isBlank()) {
                JTextArea notas = new JTextArea(orc.getObservacoesFinanceiras().trim());
                notas.setEditable(false);
                notas.setLineWrap(true);
                notas.setWrapStyleWord(true);
                notas.setOpaque(false);
                notas.setFont(notas.getFont().deriveFont(UIConstants.FONT_SMALL));
                notas.setBorder(new EmptyBorder(6, 0, 0, 0));
                panel.add(notas, BorderLayout.SOUTH);
            } else {
                panel.add(new JSeparator(), BorderLayout.SOUTH);
            }
            return panel;
        }
        private JPanel metaLabel(String k, String v) {
            JPanel p = new JPanel(new GridLayout(2, 1, 0, 2)); p.setOpaque(false);
            JLabel lk = new JLabel(k); lk.setFont(lk.getFont().deriveFont(UIConstants.FONT_SMALL)); lk.setForeground(UIManager.getColor("Label.disabledForeground"));
            JLabel lv = new JLabel(v); lv.setFont(lv.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
            p.add(lk); p.add(lv); return p;
        }

        private JPanel buildAreaLinhas() {
            JPanel panel = new JPanel(new BorderLayout(0, 8)); panel.setOpaque(false); panel.setBorder(new EmptyBorder(12, 0, 8, 0));
            JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false); toolbar.setBorder(new EmptyBorder(0, 0, 6, 0));
            JLabel titulo = new JLabel("Linhas do orçamento"); titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, UIConstants.FONT_FIELD_LABEL));
            toolbar.add(titulo, BorderLayout.WEST);
            if (!Boolean.TRUE.equals(orc.getAprovado())) {
                JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); acoes.setOpaque(false);
                JButton btnCond = buildSmallButton("Condições");
                JButton btnAdd = buildSmallButton("+ Adicionar linha"), btnRem = buildSmallButton("Remover linha");
                btnAdd.setBackground(UIConstants.COLOR_ADMIN_ACCENT); btnAdd.setForeground(Color.WHITE);
                btnCond.setForeground(UIConstants.COLOR_INFO);
                btnRem.setForeground(UIConstants.COLOR_DANGER);
                btnCond.addActionListener(e -> abrirFormCondicoes());
                btnAdd.addActionListener(e -> abrirFormLinha(null)); btnRem.addActionListener(e -> removerLinha());
                acoes.add(btnCond); acoes.add(btnRem); acoes.add(btnAdd); toolbar.add(acoes, BorderLayout.EAST);
            }
            panel.add(toolbar, BorderLayout.NORTH);

            modeloLinhas = new DefaultTableModel(COL_LINHAS, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            tabelaLinhas = new JTable(modeloLinhas);
            tabelaLinhas.setRowHeight(UIConstants.TABLE_ROW_HEIGHT); tabelaLinhas.setShowVerticalLines(false);
            tabelaLinhas.getTableHeader().setReorderingAllowed(false); tabelaLinhas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tabelaLinhas.setFillsViewportHeight(true);
            tabelaLinhas.getColumnModel().getColumn(0).setMinWidth(0); tabelaLinhas.getColumnModel().getColumn(0).setMaxWidth(0); tabelaLinhas.getColumnModel().getColumn(0).setWidth(0);
            tabelaLinhas.getColumnModel().getColumn(1).setPreferredWidth(120); tabelaLinhas.getColumnModel().getColumn(2).setPreferredWidth(180);
            tabelaLinhas.getColumnModel().getColumn(3).setPreferredWidth(80);  tabelaLinhas.getColumnModel().getColumn(4).setPreferredWidth(55);
            tabelaLinhas.getColumnModel().getColumn(5).setPreferredWidth(50);  tabelaLinhas.getColumnModel().getColumn(6).setPreferredWidth(75);
            tabelaLinhas.getColumnModel().getColumn(7).setPreferredWidth(65);  tabelaLinhas.getColumnModel().getColumn(8).setPreferredWidth(90);
            tabelaLinhas.getColumnModel().getColumn(9).setPreferredWidth(95);
            if (!Boolean.TRUE.equals(orc.getAprovado()))
                tabelaLinhas.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) abrirFormLinha(idLinhaSelecionada()); }
                });
            JScrollPane scroll = buildTablePane(tabelaLinhas); scroll.setPreferredSize(new Dimension(760, 260));
            panel.add(scroll, BorderLayout.CENTER);
            JPanel totaisPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4)); totaisPanel.setOpaque(false);
            totaisPanel.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
            lblTotal = new JLabel(); lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 13f));
            atualizarTotal(); totaisPanel.add(lblTotal); panel.add(totaisPanel, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel buildRodape() {
            JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false); panel.setBorder(new EmptyBorder(8, 0, 0, 0));
            JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); esq.setOpaque(false);
            if (!Boolean.TRUE.equals(orc.getAprovado())) {
                JButton btnAp = buildButton("Aprovar Orçamento");
                btnAp.setBackground(UIConstants.COLOR_SUCCESS); btnAp.setForeground(Color.WHITE);
                btnAp.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(dialogo, "Aprovar este orçamento? Não será possível editar as linhas após a aprovação.", "Confirmar aprovação", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try { orcamentoService.aprovar(orc.getId()); dialogo.dispose(); }
                        catch (Exception ex) { JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
                    }
                });
                esq.add(btnAp);
            }
            JButton btnPDF = buildButton("Exportar PDF");
            btnPDF.setForeground(UIConstants.COLOR_INFO);
            btnPDF.addActionListener(e -> gerarPDF(orc));
            esq.add(btnPDF);
            panel.add(esq, BorderLayout.WEST);
            JButton btnF = buildButton("Fechar"); btnF.addActionListener(e -> dialogo.dispose());
            JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); dir.setOpaque(false); dir.add(btnF);
            panel.add(dir, BorderLayout.EAST);
            return panel;
        }

        private void carregarLinhas() {
            modeloLinhas.setRowCount(0);
            for (Linhaorcamento l : linhaorcamentoService.buscarPorOrcamento(orc)) {
                String tipo  = l.getIdTipoLinhaorcamento() != null ? l.getIdTipoLinhaorcamento().getNomeTipo() : "—";
                String iva   = l.getIvaPercentagemAplicada() != null ? l.getIvaPercentagemAplicada().stripTrailingZeros().toPlainString() + "%" : "—";
                BigDecimal qty = l.getQuantidade() != null ? l.getQuantidade() : BigDecimal.ONE;
                BigDecimal pu  = l.getPrecoUnit()  != null ? l.getPrecoUnit()  : BigDecimal.ZERO;
                BigDecimal desc = l.getDescontoPercentagem() != null ? l.getDescontoPercentagem() : BigDecimal.ZERO;
                BigDecimal base = CalculosFinanceiros.subtotalSemIva(l);
                BigDecimal tot = CalculosFinanceiros.totalComIva(l);
                String[] partes  = extrairGrupoEDescricao(l.getNome());
                String grupo     = partes[0]; // pode ser "" se não tiver grupo
                String descricao = partes[1];
                modeloLinhas.addRow(new Object[]{ l.getId(), grupo, descricao, tipo, iva,
                        qty.stripTrailingZeros().toPlainString(), pu.setScale(2, RoundingMode.HALF_UP) + " €",
                        desc.stripTrailingZeros().toPlainString() + "%",
                        base.setScale(2, RoundingMode.HALF_UP) + " €",
                        tot.setScale(2, RoundingMode.HALF_UP) + " €" });
            }
            atualizarTotal();
        }

        /** Extrai grupo e descrição do campo nome. Formato: "[Grupo] Descrição" ou só "Descrição". */
        private String[] extrairGrupoEDescricao(String nome) {
            if (nome == null) return new String[]{"", "—"};
            if (nome.startsWith("[")) {
                int fim = nome.indexOf(']');
                if (fim > 1) {
                    String grupo = nome.substring(1, fim).trim();
                    String desc  = nome.substring(fim + 1).trim();
                    return new String[]{grupo, desc.isBlank() ? "—" : desc};
                }
            }
            return new String[]{"", nome};
        }
        private void atualizarTotal() {
            if (lblTotal == null) return;
            TotaisFinanceiros totais = orcamentoService.calcularTotais(orc);
            BigDecimal base = baseTributavel(totais);
            lblTotal.setText("<html>Bruto s/IVA: <b>" + fe(totais.subtotalSemIva()) + "</b>"
                    + " &nbsp;&nbsp; Desc.: <b>" + fe(totais.valorDesconto()) + "</b>"
                    + " &nbsp;&nbsp; Base: <b>" + fe(base) + "</b>"
                    + " &nbsp;&nbsp; IVA: <b>" + fe(totais.valorIva()) + "</b>"
                    + " &nbsp;&nbsp; Total: <b>" + fe(totais.totalComIva()) + "</b></html>");
        }
        private Integer idLinhaSelecionada() {
            int row = tabelaLinhas.getSelectedRow(); if (row < 0) return null;
            return (Integer) modeloLinhas.getValueAt(tabelaLinhas.convertRowIndexToModel(row), 0);
        }
        private void removerLinha() {
            Integer id = idLinhaSelecionada();
            if (id == null) { JOptionPane.showMessageDialog(dialogo, "Seleciona uma linha.", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
            if (JOptionPane.showConfirmDialog(dialogo, "Remover esta linha?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                try { linhaorcamentoService.eliminar(id); carregarLinhas(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }

        private void abrirFormCondicoes() {
            JDialog dlg = criarDialogo("Condições do Orçamento");
            FormOrcamento form = new FormOrcamento(dlg, orc, List.of(orc.getIdObra()));
            dlg.add(form);
            dlg.pack();
            dlg.setLocationRelativeTo(dialogo);
            dlg.setVisible(true);
            if (form.confirmado()) {
                try {
                    orcamentoService.guardar(form.construir());
                    dialogo.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // ── Form de linha ─────────────────────────────────────────────────────
        private void abrirFormLinha(Integer idLinha) {
            Linhaorcamento ex = idLinha != null ? linhaorcamentoService.buscarPorId(idLinha) : null;

            // Recolher grupos já existentes neste orçamento
            java.util.LinkedHashSet<String> gruposExistentes = new java.util.LinkedHashSet<>();
            for (Linhaorcamento l : linhaorcamentoService.buscarPorOrcamento(orc)) {
                String[] partes = extrairGrupoEDescricao(l.getNome());
                if (!partes[0].isBlank()) gruposExistentes.add(partes[0]);
            }

            Window owner = SwingUtilities.getWindowAncestor(dialogo);
            JDialog dlgL = owner instanceof Frame
                    ? new JDialog((Frame) owner, ex == null ? "Nova linha" : "Editar linha", true)
                    : new JDialog((Dialog) owner, ex == null ? "Nova linha" : "Editar linha", true);
            dlgL.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlgL.setResizable(true);

            FormLinha form = new FormLinha(dlgL, ex, gruposExistentes);

            // Envolver em JScrollPane — garante que a label nunca sai do viewport
            JScrollPane scroll = new JScrollPane(form);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            dlgL.setContentPane(scroll);
            dlgL.pack();

            // Limitar a 90% da altura do ecrã
            int maxH = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
            if (dlgL.getHeight() > maxH) dlgL.setSize(dlgL.getWidth(), maxH);

            dlgL.setLocationRelativeTo(dialogo);
            dlgL.setVisible(true);

            if (form.confirmado()) {
                try {
                    Linhaorcamento linha = form.construir();
                    linha.setIdOrcamento(orc);
                    if (ex != null) linha.setId(ex.getId());
                    linhaorcamentoService.guardar(linha);
                    carregarLinhas();
                } catch (Exception e) { JOptionPane.showMessageDialog(dialogo, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
            }
        }
    }

    // =========================================================================
    //  FORMULÁRIO DE LINHA — label sempre visível, nunca cortado
    // =========================================================================
    private class FormLinha extends JPanel {
        private boolean confirmado = false;
        private final JComboBox<String>             comboGrupo;
        private final JTextField                    campoNome;
        private final JComboBox<Tipolinhaorcamento> comboTipo;
        private final JComboBox<Material>           comboMaterial;
        private final JComboBox<Taxaiva>            comboIva;
        private final JTextField                    campoQtd;
        private final JTextField                    campoPreco;
        private final JTextField                    campoDesconto;
        private final JLabel                        lblErro;

        FormLinha(JDialog dialogo, Linhaorcamento existente, java.util.Set<String> gruposExistentes) {
            setLayout(new BorderLayout());

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            corpo.setBorder(new EmptyBorder(20, 24, 8, 24));
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.gridx = 0;

            // Combo editável de grupos
            comboGrupo = new JComboBox<>();
            comboGrupo.setEditable(true);
            comboGrupo.addItem(""); // opção vazia (sem grupo)
            gruposExistentes.forEach(g -> { if (!g.isBlank()) comboGrupo.addItem(g); });

            campoNome  = new JTextField();
            campoQtd   = new JTextField();
            campoPreco = new JTextField();
            campoDesconto = new JTextField();

            List<Tipolinhaorcamento> tipos = tipoService.listarTodos();
            comboTipo = new JComboBox<>(tipos.toArray(new Tipolinhaorcamento[0]));
            comboTipo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(l, v, i, s, f);
                    if (v instanceof Tipolinhaorcamento t) setText(t.getNomeTipo()); return this;
                }
            });

            List<Material> materiais = materialService.listarTodos();
            comboMaterial = new JComboBox<>();
            comboMaterial.addItem(null);
            for (Material m : materiais) comboMaterial.addItem(m);
            comboMaterial.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(l, v, i, s, f);
                    if (v == null) setText("Sem material associado");
                    else if (v instanceof Material m) setText(m.getNome() + " (Stock: " + (m.getStockAtual() != null ? m.getStockAtual() : 0) + ")");
                    return this;
                }
            });

            List<Taxaiva> taxas = taxaivaService.listarTodos();
            comboIva = new JComboBox<>(taxas.toArray(new Taxaiva[0]));
            comboIva.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(l, v, i, s, f);
                    if (v instanceof Taxaiva t) setText(t.getDescricao()); return this;
                }
            });

            if (existente != null) {
                String[] partes = extrairGrupoEDescricao(existente.getNome());
                comboGrupo.setSelectedItem(partes[0]);
                campoNome.setText(partes[1].equals("—") ? "" : partes[1]);
                campoQtd.setText(existente.getQuantidade() != null ? existente.getQuantidade().stripTrailingZeros().toPlainString() : "1");
                campoPreco.setText(existente.getPrecoUnit() != null ? existente.getPrecoUnit().toPlainString() : "");
                campoDesconto.setText(existente.getDescontoPercentagem() != null
                        ? existente.getDescontoPercentagem().stripTrailingZeros().toPlainString()
                        : "0");
                if (existente.getIdTipoLinhaorcamento() != null)
                    for (int i = 0; i < tipos.size(); i++) if (tipos.get(i).getId().equals(existente.getIdTipoLinhaorcamento().getId())) { comboTipo.setSelectedIndex(i); break; }
                if (existente.getIdMaterial() != null) {
                    for (int i = 0; i < comboMaterial.getItemCount(); i++) {
                        Material m = comboMaterial.getItemAt(i);
                        if (m != null && m.getId().equals(existente.getIdMaterial().getId())) {
                            comboMaterial.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                if (existente.getIdIva() != null)
                    for (int i = 0; i < taxas.size(); i++) if (taxas.get(i).getId().equals(existente.getIdIva().getId())) { comboIva.setSelectedIndex(i); break; }
            } else {
                campoQtd.setText("1");
                campoDesconto.setText("0");
            }

            comboMaterial.addActionListener(e -> {
                Object sel = comboMaterial.getSelectedItem();
                if (!(sel instanceof Material m)) return;
                if (campoNome.getText().trim().isBlank()) campoNome.setText(m.getNome());
            });

            // Nota explicativa sobre grupos
            JLabel lblDica = new JLabel("<html><i>Escolhe um grupo existente ou escreve um novo. Linhas do mesmo grupo aparecem agrupadas no PDF do cliente.</i></html>");
            lblDica.setFont(lblDica.getFont().deriveFont(UIConstants.FONT_SMALL));
            lblDica.setForeground(UIManager.getColor("Label.disabledForeground"));
            GridBagConstraints cDica = new GridBagConstraints();
            cDica.fill = GridBagConstraints.HORIZONTAL; cDica.weightx = 1.0; cDica.gridx = 0;
            cDica.gridy = 0; cDica.insets = new Insets(0, 0, 8, 0);
            corpo.add(lblDica, cDica);

            addField(corpo, c, 1, "Grupo / Janela  (opcional)", comboGrupo);
            addField(corpo, c, 2, "Descrição do item *",        campoNome);
            addField(corpo, c, 3, "Material (opcional)",        comboMaterial);
            addField(corpo, c, 4, "Tipo de linha",              comboTipo);
            addField(corpo, c, 5, "Taxa de IVA",                comboIva);
            addField(corpo, c, 6, "Quantidade *",               campoQtd);
            addField(corpo, c, 7, "Preço unitário (€) *",       campoPreco);
            addField(corpo, c, 8, "Desconto (%)",               campoDesconto);

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = 16; c.insets = new Insets(6, 0, 0, 0); corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rod = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rod.setOpaque(false); rod.setBorder(new EmptyBorder(4, 24, 16, 24));
            JButton btnC = buildButton("Cancelar"), btnG = buildButton(existente == null ? "Adicionar" : "Guardar");
            btnG.setBackground(UIConstants.COLOR_ADMIN_ACCENT); btnG.setForeground(Color.WHITE);
            btnC.addActionListener(e -> dialogo.dispose());
            btnG.addActionListener(e -> { if (validar()) { confirmado = true; dialogo.dispose(); } });
            rod.add(btnC); rod.add(btnG);
            add(rod, BorderLayout.SOUTH);

            setMinimumSize(new Dimension(460, 0));
            setPreferredSize(new Dimension(460, getPreferredSize().height));
        }

        private String[] extrairGrupoEDescricao(String nome) {
            if (nome == null) return new String[]{"", "—"};
            if (nome.startsWith("[")) {
                int fim = nome.indexOf(']');
                if (fim > 1) {
                    String grupo = nome.substring(1, fim).trim();
                    String desc  = nome.substring(fim + 1).trim();
                    return new String[]{grupo, desc.isBlank() ? "—" : desc};
                }
            }
            return new String[]{"", nome};
        }

        private void addField(JPanel panel, GridBagConstraints c, int fi, String lbl, JComponent campo) {
            JLabel label = new JLabel(lbl);
            label.setFont(label.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
            c.gridy  = fi * 2;
            c.insets = new Insets(fi <= 1 ? 0 : 10, 0, 2, 0);
            panel.add(label, c);
            c.gridy  = fi * 2 + 1;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(campo, c);
        }

        private boolean validar() {
            if (campoNome.getText().trim().isBlank()) { lblErro.setText("A descrição do item é obrigatória."); return false; }
            try { if (new BigDecimal(campoQtd.getText().trim().replace(',', '.')).signum() <= 0) { lblErro.setText("Quantidade deve ser maior que zero."); return false; } }
            catch (NumberFormatException e) { lblErro.setText("Quantidade inválida."); return false; }
            try { if (new BigDecimal(campoPreco.getText().trim().replace(',', '.')).signum() <= 0) { lblErro.setText("Preço deve ser maior que zero."); return false; } }
            catch (NumberFormatException e) { lblErro.setText("Preço inválido."); return false; }
            try {
                BigDecimal desconto = new BigDecimal(campoDesconto.getText().trim().replace(',', '.'));
                if (desconto.compareTo(BigDecimal.ZERO) < 0 || desconto.compareTo(new BigDecimal("100")) >= 0) {
                    lblErro.setText("O desconto tem de estar entre 0 e 99,99%.");
                    return false;
                }
            } catch (NumberFormatException e) {
                lblErro.setText("Desconto inválido.");
                return false;
            }
            lblErro.setText(" "); return true;
        }
        boolean confirmado() { return confirmado; }
        Linhaorcamento construir() {
            Linhaorcamento l = new Linhaorcamento();
            Object selGrupo = comboGrupo.getSelectedItem();
            String grupo = selGrupo != null ? selGrupo.toString().trim() : "";
            String desc  = campoNome.getText().trim();
            l.setNome(grupo.isBlank() ? desc : "[" + grupo + "] " + desc);
            l.setQuantidade(new BigDecimal(campoQtd.getText().trim().replace(',', '.')));
            l.setPrecoUnit(new BigDecimal(campoPreco.getText().trim().replace(',', '.')));
            l.setDescontoPercentagem(new BigDecimal(campoDesconto.getText().trim().replace(',', '.')));
            if (comboMaterial.getSelectedItem() instanceof Material m) {
                l.setIdMaterial(m);
            }
            if (comboTipo.getSelectedItem() instanceof Tipolinhaorcamento t) l.setIdTipoLinhaorcamento(t);
            if (comboIva.getSelectedItem() instanceof Taxaiva taxa) { l.setIdIva(taxa); l.setIvaPercentagemAplicada(taxa.getPercentagem()); }
            return l;
        }
    }

}
