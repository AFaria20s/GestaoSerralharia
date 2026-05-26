package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StockPanel extends BasePanel {

    // ── Serviços ──────────────────────────────────────────────────────────────
    private final MaterialService       materialService;
    private final FornecedorService     fornecedorService;
    private final EncomendaService      encomendaService;
    private final LinhaencomendaService linhaencomendaService;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JTabbedPane tabs;

    // ── Tab Materiais ─────────────────────────────────────────────────────────
    private JTextField        campoPesquisaMat;
    private JCheckBox         chkStockBaixo;
    private JLabel            lblContadorMat;
    private DefaultTableModel modeloMat;
    private JTable            tabelaMat;
    private List<Material>    materiaisCarregados;

    // ── Tab Fornecedores ──────────────────────────────────────────────────────
    private JTextField        campoPesquisaForn;
    private JLabel            lblContadorForn;
    private DefaultTableModel modeloForn;
    private JTable            tabelaForn;
    private List<Fornecedor>  fornecedoresCarregados;

    // ── Tab Encomendas ────────────────────────────────────────────────────────
    private JComboBox<String> filtroEncomenda;
    private JLabel            lblContadorEnc;
    private DefaultTableModel modeloEnc;
    private JTable            tabelaEnc;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int LIMIAR_STOCK_BAIXO = 50;

    // ─────────────────────────────────────────────────────────────────────────

    public StockPanel(MaterialService materialService,
                      FornecedorService fornecedorService,
                      EncomendaService encomendaService,
                      LinhaencomendaService linhaencomendaService) {
        this.materialService       = materialService;
        this.fornecedorService     = fornecedorService;
        this.encomendaService      = encomendaService;
        this.linhaencomendaService = linhaencomendaService;

        add(buildHeader("Stock", ""), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(13f));
        tabs.addTab("Materiais",    buildTabMateriais());
        tabs.addTab("Fornecedores", buildTabFornecedores());
        tabs.addTab("Encomendas",   buildTabEncomendas());

        add(tabs, BorderLayout.CENTER);
        carregarTudo();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARREGAMENTO GLOBAL
    // ─────────────────────────────────────────────────────────────────────────

    private void carregarTudo() {
        carregarMateriais();
        carregarFornecedores();
        carregarEncomendas();
    }

    // =========================================================================
    //  TAB — MATERIAIS
    // =========================================================================

    private JPanel buildTabMateriais() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 2, 2, 2));

        // ── Barra de filtros ─────────────────────────────────────────────────
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        campoPesquisaMat = buildSearchField("Pesquisar por nome...");
        campoPesquisaMat.getDocument().addDocumentListener(docListener(this::aplicarFiltroMat));

        chkStockBaixo = new JCheckBox("Só stock baixo (< " + LIMIAR_STOCK_BAIXO + ")");
        chkStockBaixo.setOpaque(false);
        chkStockBaixo.setFont(chkStockBaixo.getFont().deriveFont(12f));
        chkStockBaixo.addActionListener(e -> aplicarFiltroMat());

        lblContadorMat = contadorLabel();

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setOpaque(false);
        esq.add(campoPesquisaMat);
        esq.add(chkStockBaixo);
        esq.add(lblContadorMat);
        barra.add(esq, BorderLayout.WEST);

        // ── Tabela ───────────────────────────────────────────────────────────
        modeloMat = new DefaultTableModel(
                new String[]{"ID", "Nome", "Fornecedor", "Stock", "Reservado", "Disponível"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        tabelaMat = new JTable(modeloMat);
        ocultarColuna(tabelaMat, 0);
        tabelaMat.getColumnModel().getColumn(1).setPreferredWidth(240);
        tabelaMat.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabelaMat.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaMat.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabelaMat.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabelaMat.getColumnModel().getColumn(3).setCellRenderer(new StockCellRenderer());
        tabelaMat.addMouseListener(dblClick(this::abrirDetalheOuEditarMaterial));

        JScrollPane scroll = buildTablePane(tabelaMat);

        // ── Barra de acções ──────────────────────────────────────────────────
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);

        JButton btnNovo    = buildSmallButton("Novo Material");
        JButton btnAjustar = buildSmallButton("Ajustar Stock");
        JButton btnElim    = buildSmallButton("Eliminar");
        btnElim.setForeground(UIConstants.COLOR_DANGER);

        btnNovo   .addActionListener(e -> abrirDialogoNovoMaterial());
        btnAjustar.addActionListener(e -> abrirDialogoAjustarStock());
        btnElim   .addActionListener(e -> eliminarMaterial());

        acoes.add(btnNovo);
        acoes.add(btnAjustar);
        acoes.add(Box.createHorizontalStrut(12));
        acoes.add(btnElim);

        panel.add(barra,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(acoes,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Dados materiais ───────────────────────────────────────────────────────

    private void carregarMateriais() {
        materiaisCarregados = materialService.listarTodos();
        aplicarFiltroMat();
    }

    private void aplicarFiltroMat() {
        if (materiaisCarregados == null) return;
        modeloMat.setRowCount(0);
        String q = campoPesquisaMat != null ? campoPesquisaMat.getText().trim().toLowerCase() : "";
        boolean apenasStockBaixo = chkStockBaixo != null && chkStockBaixo.isSelected();
        int count = 0;
        for (Material m : materiaisCarregados) {
            if (!q.isEmpty() && !m.getNome().toLowerCase().contains(q)) continue;
            if (apenasStockBaixo && materialService.stockDisponivel(m).compareTo(BigDecimal.valueOf(LIMIAR_STOCK_BAIXO)) >= 0) continue;
            String forn = m.getIdFornecedor() != null ? m.getIdFornecedor().getNome() : "—";
            BigDecimal reservado = materialService.stockReservado(m).setScale(2, RoundingMode.HALF_UP);
            BigDecimal disponivel = materialService.stockDisponivel(m).setScale(2, RoundingMode.HALF_UP);
            modeloMat.addRow(new Object[]{m.getId(), m.getNome(), forn, m.getStockAtual(), reservado.toPlainString(), disponivel.toPlainString()});
            count++;
        }
        if (lblContadorMat != null) lblContadorMat.setText(count + " material(is)");
    }

    private Material materialSelecionado() {
        int row = tabelaMat.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) modeloMat.getValueAt(tabelaMat.convertRowIndexToModel(row), 0);
        try { return materialService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(ex.getMessage()); return null; }
    }

    // ── Diálogos materiais ────────────────────────────────────────────────────

    private void abrirDialogoNovoMaterial() {
        JDialog dlg = criarDialogo("Novo Material");
        dlg.setSize(420, 280);
        dlg.setLocationRelativeTo(this);

        JPanel content = painelConteudo();
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JTextField campoNome  = new JTextField();
        JTextField campoStock = new JTextField("0");

        List<Fornecedor> fList = fornecedorService.listarTodos();
        JComboBox<Fornecedor> comboForn = comboFornecedor(fList);

        JLabel lblErro = erroLabel();

        adicionarCampo(form, 0, "Nome *",        campoNome);
        adicionarCampo(form, 1, "Fornecedor",    comboForn);
        adicionarCampo(form, 2, "Stock inicial", campoStock);
        adicionarErro(form, 3, lblErro);

        JButton btnGuardar  = btnPrimario("Criar");
        JButton btnCancelar = buildButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String nome = campoNome.getText().trim();
                if (nome.isBlank()) throw new IllegalArgumentException("O nome é obrigatório.");
                int stock = Integer.parseInt(campoStock.getText().trim());
                Material m = new Material();
                m.setNome(nome);
                m.setStockAtual(stock);
                if (comboForn.getSelectedItem() instanceof Fornecedor f) m.setIdFornecedor(f);
                materialService.guardar(m);
                dlg.dispose();
                carregarMateriais();
            } catch (NumberFormatException ex) {
                lblErro.setText("Stock deve ser um número inteiro.");
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(rodape(dlg, btnCancelar, btnGuardar), BorderLayout.SOUTH);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void abrirDetalheOuEditarMaterial() {
        Material m = materialSelecionado();
        if (m == null) return;

        JDialog dlg = criarDialogo("Editar Material — " + m.getNome());
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);

        JPanel content = painelConteudo();
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JTextField campoNome = new JTextField(m.getNome());

        List<Fornecedor> fList = fornecedorService.listarTodos();
        JComboBox<Fornecedor> comboForn = comboFornecedor(fList);
        if (m.getIdFornecedor() != null) {
            for (int i = 0; i < fList.size(); i++) {
                if (fList.get(i).getId().equals(m.getIdFornecedor().getId())) {
                    comboForn.setSelectedIndex(i); break;
                }
            }
        }

        JLabel lblErro = erroLabel();
        adicionarCampo(form, 0, "Nome *",     campoNome);
        adicionarCampo(form, 1, "Fornecedor", comboForn);
        adicionarErro(form, 2, lblErro);

        // Stock é só leitura aqui — é ajustado pelo botão dedicado
        JLabel lblStockInfo = new JLabel("Stock actual: " + m.getStockAtual()
                + "   (para ajustar use «± Ajustar Stock»)");
        lblStockInfo.setFont(lblStockInfo.getFont().deriveFont(UIConstants.FONT_SMALL));
        lblStockInfo.setForeground(UIManager.getColor("Label.disabledForeground"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 0, 0);
        form.add(lblStockInfo, gbc);

        JButton btnGuardar  = btnPrimario("Guardar");
        JButton btnCancelar = buildButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String nome = campoNome.getText().trim();
                if (nome.isBlank()) throw new IllegalArgumentException("O nome é obrigatório.");
                m.setNome(nome);
                if (comboForn.getSelectedItem() instanceof Fornecedor f) m.setIdFornecedor(f);
                else m.setIdFornecedor(null);
                materialService.guardar(m);
                dlg.dispose();
                carregarMateriais();
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(rodape(dlg, btnCancelar, btnGuardar), BorderLayout.SOUTH);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void abrirDialogoAjustarStock() {
        Material m = materialSelecionado();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Selecciona um material primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = criarDialogo("Ajustar Stock — " + m.getNome());
        dlg.setSize(380, 240);
        dlg.setLocationRelativeTo(this);

        JPanel content = painelConteudo();
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JLabel lblActual = new JLabel("Stock actual: " + m.getStockAtual());
        lblActual.setFont(lblActual.getFont().deriveFont(Font.BOLD, 13f));
        GridBagConstraints g0 = new GridBagConstraints();
        g0.gridy = 0; g0.gridx = 0; g0.gridwidth = 2;
        g0.fill = GridBagConstraints.HORIZONTAL; g0.insets = new Insets(0, 0, 12, 0);
        form.add(lblActual, g0);

        // Tipo de ajuste: entrada / saída
        JRadioButton rdEntrada = new JRadioButton("Entrada (+)", true);
        JRadioButton rdSaida   = new JRadioButton("Saída (−)");
        rdEntrada.setOpaque(false);
        rdSaida.setOpaque(false);
        ButtonGroup grp = new ButtonGroup();
        grp.add(rdEntrada); grp.add(rdSaida);

        JPanel rdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rdPanel.setOpaque(false);
        rdPanel.add(rdEntrada);
        rdPanel.add(rdSaida);

        GridBagConstraints g1 = new GridBagConstraints();
        g1.gridy = 1; g1.gridx = 0; g1.gridwidth = 2;
        g1.fill = GridBagConstraints.HORIZONTAL; g1.insets = new Insets(0, 0, 6, 0);
        form.add(rdPanel, g1);

        JTextField campoQtd = new JTextField();
        JLabel     lblErro  = erroLabel();
        adicionarCampo(form, 2, "Quantidade *", campoQtd);
        adicionarErro(form, 3, lblErro);

        JButton btnAplicar  = btnPrimario("Aplicar");
        JButton btnCancelar = buildButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        btnAplicar.addActionListener(e -> {
            try {
                int qtd = Integer.parseInt(campoQtd.getText().trim());
                if (qtd <= 0) throw new IllegalArgumentException("A quantidade deve ser positiva.");
                int novoStock = rdEntrada.isSelected()
                        ? m.getStockAtual() + qtd
                        : m.getStockAtual() - qtd;
                if (novoStock < 0) throw new IllegalArgumentException(
                        "Saldo insuficiente — stock ficaria em " + novoStock + ".");
                m.setStockAtual(novoStock);
                materialService.guardar(m);
                dlg.dispose();
                carregarMateriais();
                JOptionPane.showMessageDialog(this,
                        "Stock actualizado para " + novoStock + " unidades.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                lblErro.setText("Introduz um número inteiro válido.");
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(rodape(dlg, btnCancelar, btnAplicar), BorderLayout.SOUTH);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void eliminarMaterial() {
        Material m = materialSelecionado();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Selecciona um material primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Eliminar o material \"" + m.getNome() + "\"?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        try {
            materialService.eliminar(m.getId());
            carregarMateriais();
        } catch (Exception ex) {
            mostrarErro("Não foi possível eliminar: " + ex.getMessage());
        }
    }

    // =========================================================================
    //  TAB — FORNECEDORES
    // =========================================================================

    private JPanel buildTabFornecedores() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 2, 2, 2));

        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        campoPesquisaForn = buildSearchField("Pesquisar por nome…");
        campoPesquisaForn.getDocument().addDocumentListener(docListener(this::aplicarFiltroForn));

        lblContadorForn = contadorLabel();

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setOpaque(false);
        esq.add(campoPesquisaForn);
        esq.add(lblContadorForn);
        barra.add(esq, BorderLayout.WEST);

        // ── Tabela ───────────────────────────────────────────────────────────
        modeloForn = new DefaultTableModel(
                new String[]{"ID", "Nome", "NIF", "Email", "Contacto"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        tabelaForn = new JTable(modeloForn);
        ocultarColuna(tabelaForn, 0);
        tabelaForn.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabelaForn.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabelaForn.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabelaForn.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabelaForn.addMouseListener(dblClick(this::abrirEditarFornecedor));

        JScrollPane scroll = buildTablePane(tabelaForn);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);

        JButton btnNovo  = buildSmallButton("+ Novo Fornecedor");
        JButton btnEditar = buildSmallButton("Editar");
        JButton btnElim  = buildSmallButton("Eliminar");
        btnElim.setForeground(UIConstants.COLOR_DANGER);

        btnNovo  .addActionListener(e -> abrirDialogoNovoFornecedor());
        btnEditar.addActionListener(e -> abrirEditarFornecedor());
        btnElim  .addActionListener(e -> eliminarFornecedor());

        acoes.add(btnNovo);
        acoes.add(btnEditar);
        acoes.add(Box.createHorizontalStrut(12));
        acoes.add(btnElim);

        panel.add(barra,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(acoes,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Dados fornecedores ────────────────────────────────────────────────────

    private void carregarFornecedores() {
        fornecedoresCarregados = fornecedorService.listarTodos();
        aplicarFiltroForn();
    }

    private void aplicarFiltroForn() {
        if (fornecedoresCarregados == null) return;
        modeloForn.setRowCount(0);
        String q = campoPesquisaForn != null ? campoPesquisaForn.getText().trim().toLowerCase() : "";
        int count = 0;
        for (Fornecedor f : fornecedoresCarregados) {
            if (!q.isEmpty() && !f.getNome().toLowerCase().contains(q)) continue;
            modeloForn.addRow(new Object[]{f.getId(), f.getNome(), f.getNif(), f.getEmail(), f.getContacto()});
            count++;
        }
        if (lblContadorForn != null) lblContadorForn.setText(count + " fornecedor(es)");
    }

    private Fornecedor fornecedorSelecionado() {
        int row = tabelaForn.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) modeloForn.getValueAt(tabelaForn.convertRowIndexToModel(row), 0);
        return fornecedorService.buscarPorId(id);
    }

    // ── Diálogos fornecedores ─────────────────────────────────────────────────

    private void abrirDialogoNovoFornecedor() {
        abrirFormFornecedor(null);
    }

    private void abrirEditarFornecedor() {
        Fornecedor f = fornecedorSelecionado();
        if (f == null) {
            JOptionPane.showMessageDialog(this, "Selecciona um fornecedor primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        abrirFormFornecedor(f);
    }

    private void abrirFormFornecedor(Fornecedor existente) {
        boolean isNovo = existente == null;
        JDialog dlg = criarDialogo(isNovo ? "Novo Fornecedor" : "Editar Fornecedor");
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);

        JPanel content = painelConteudo();
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JTextField campoNome     = new JTextField(isNovo ? "" : existente.getNome());
        JTextField campoNif      = new JTextField(isNovo ? "" : existente.getNif());
        JTextField campoEmail    = new JTextField(isNovo ? "" : existente.getEmail());
        JTextField campoContacto = new JTextField(isNovo ? "" : existente.getContacto());
        JLabel lblErro = erroLabel();

        adicionarCampo(form, 0, "Nome *",      campoNome);
        adicionarCampo(form, 1, "NIF *",       campoNif);
        adicionarCampo(form, 2, "Email *",     campoEmail);
        adicionarCampo(form, 3, "Contacto *",  campoContacto);
        adicionarErro(form, 4, lblErro);

        JButton btnGuardar  = btnPrimario(isNovo ? "Criar" : "Guardar");
        JButton btnCancelar = buildButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String nome     = campoNome.getText().trim();
                String nif      = campoNif.getText().trim();
                String email    = campoEmail.getText().trim();
                String contacto = campoContacto.getText().trim();
                if (nome.isBlank())     throw new IllegalArgumentException("O nome é obrigatório.");
                if (nif.isBlank())      throw new IllegalArgumentException("O NIF é obrigatório.");
                if (email.isBlank() || !email.contains("@"))
                    throw new IllegalArgumentException("Introduz um email válido.");
                if (contacto.isBlank()) throw new IllegalArgumentException("O contacto é obrigatório.");

                Fornecedor f = isNovo ? new Fornecedor() : existente;
                f.setNome(nome);
                f.setNif(nif);
                f.setEmail(email);
                f.setContacto(contacto);
                fornecedorService.guardar(f);
                dlg.dispose();
                carregarFornecedores();
                // Recarregar combos de material se existirem
                carregarMateriais();
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(rodape(dlg, btnCancelar, btnGuardar), BorderLayout.SOUTH);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void eliminarFornecedor() {
        Fornecedor f = fornecedorSelecionado();
        if (f == null) {
            JOptionPane.showMessageDialog(this, "Selecciona um fornecedor primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Eliminar o fornecedor \"" + f.getNome() + "\"?\n" +
                        "Os materiais associados também serão eliminados.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        try {
            fornecedorService.eliminar(f.getId());
            carregarFornecedores();
            carregarMateriais();
        } catch (Exception ex) {
            mostrarErro("Não foi possível eliminar: " + ex.getMessage());
        }
    }

    // =========================================================================
    //  TAB — ENCOMENDAS
    // =========================================================================

    private JPanel buildTabEncomendas() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 2, 2, 2));

        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        filtroEncomenda = new JComboBox<>(new String[]{"Todas", "Pendentes", "Entregues"});
        filtroEncomenda.setPreferredSize(new Dimension(150, 30));
        filtroEncomenda.addActionListener(e -> carregarEncomendas());

        lblContadorEnc = contadorLabel();

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setOpaque(false);
        esq.add(new JLabel("Filtrar:"));
        esq.add(filtroEncomenda);
        esq.add(lblContadorEnc);
        barra.add(esq, BorderLayout.WEST);

        // ── Tabela ───────────────────────────────────────────────────────────
        modeloEnc = new DefaultTableModel(
                new String[]{"ID", "Fornecedor", "Data Pedido", "Total (€)", "Estado"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        tabelaEnc = new JTable(modeloEnc);
        ocultarColuna(tabelaEnc, 0);
        tabelaEnc.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabelaEnc.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabelaEnc.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelaEnc.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabelaEnc.getColumnModel().getColumn(4).setCellRenderer(new EstadoEncCellRenderer());
        tabelaEnc.addMouseListener(dblClick(this::abrirDetalheEncomenda));

        JScrollPane scroll = buildTablePane(tabelaEnc);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);

        JButton btnNova    = buildSmallButton("+ Nova Encomenda");
        JButton btnDetalhe = buildSmallButton("Ver Detalhe");
        JButton btnEntreg  = buildSmallButton("Marcar Entregue");
        JButton btnElim    = buildSmallButton("Eliminar");
        btnEntreg.setForeground(UIConstants.COLOR_SUCCESS);
        btnElim  .setForeground(UIConstants.COLOR_DANGER);

        btnNova   .addActionListener(e -> abrirDialogoNovaEncomenda());
        btnDetalhe.addActionListener(e -> abrirDetalheEncomenda());
        btnEntreg .addActionListener(e -> marcarEntregue());
        btnElim   .addActionListener(e -> eliminarEncomenda());

        acoes.add(btnNova);
        acoes.add(btnDetalhe);
        acoes.add(btnEntreg);
        acoes.add(Box.createHorizontalStrut(12));
        acoes.add(btnElim);

        panel.add(barra,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(acoes,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Dados encomendas ──────────────────────────────────────────────────────

    private void carregarEncomendas() {
        if (modeloEnc == null) return;
        modeloEnc.setRowCount(0);
        String filtro = filtroEncomenda != null ? (String) filtroEncomenda.getSelectedItem() : "Todas";

        List<Encomenda> lista = switch (filtro) {
            case "Pendentes"  -> encomendaService.buscarPorEntregue(false);
            case "Entregues"  -> encomendaService.buscarPorEntregue(true);
            default           -> encomendaService.listarTodos();
        };

        for (Encomenda e : lista) {
            String forn   = e.getIdFornecedor() != null ? e.getIdFornecedor().getNome() : "—";
            String data   = e.getDataPedido() != null ? e.getDataPedido().format(FMT) : "—";
            String total  = e.getValorTotalCompra() != null
                    ? String.format("%.2f €", e.getValorTotalCompra()) : "0.00 €";
            String estado = Boolean.TRUE.equals(e.getEntregue()) ? "Entregue" : "Pendente";
            modeloEnc.addRow(new Object[]{e.getId(), forn, data, total, estado});
        }
        if (lblContadorEnc != null) lblContadorEnc.setText(lista.size() + " encomenda(s)");
    }

    private Encomenda encomendaSelecionada() {
        int row = tabelaEnc.getSelectedRow();
        if (row < 0) return null;
        Integer id = (Integer) modeloEnc.getValueAt(tabelaEnc.convertRowIndexToModel(row), 0);
        try { return encomendaService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(ex.getMessage()); return null; }
    }

    // ── Diálogos encomendas ───────────────────────────────────────────────────

    /**
     * Criar nova encomenda: selecciona fornecedor, depois adiciona linhas
     * (material + quantidade + preço unitário) antes de guardar.
     */
    private void abrirDialogoNovaEncomenda() {
        List<Fornecedor> fornecedores = fornecedorService.listarTodos();
        if (fornecedores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não existem fornecedores registados.\nCria um fornecedor primeiro no separador Fornecedores.",
                    "Sem fornecedores", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = criarDialogo("Nova Encomenda");
        dlg.setSize(680, 520);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(20, 24, 16, 24));
        content.setBackground(UIManager.getColor("Panel.background"));

        // ── Fornecedor + data ─────────────────────────────────────────────
        JPanel topForm = new JPanel(new GridBagLayout());
        topForm.setOpaque(false);

        JComboBox<Fornecedor> comboForn = comboFornecedor(fornecedores);

        adicionarCampo(topForm, 0, "Fornecedor *", comboForn);

        // ── Tabela de linhas (ao vivo) ────────────────────────────────────
        DefaultTableModel modeloLinhas = new DefaultTableModel(
                new String[]{"Material", "Qtd", "Preço Unit. (€)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblLinhas = new JTable(modeloLinhas);
        tblLinhas.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tblLinhas.setShowVerticalLines(false);
        tblLinhas.setFillsViewportHeight(true);
        tblLinhas.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblLinhas.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblLinhas.getColumnModel().getColumn(2).setPreferredWidth(120);
        JScrollPane scrollLinhas = buildTablePane(tblLinhas);
        scrollLinhas.setPreferredSize(new Dimension(0, 200));

        // Lista paralela para guardar os dados das linhas
        List<Object[]> linhasTemp = new ArrayList<>(); // {Material, Integer qtd, BigDecimal preco}

        JLabel lblTotal = new JLabel("Total: 0.00 €");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 13f));

        // ── Linha de adição ───────────────────────────────────────────────
        List<Material> materiais = materialService.listarTodos();
        JComboBox<Material> comboMat = new JComboBox<>(materiais.toArray(new Material[0]));
        comboMat.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Material mat) setText(mat.getNome() + "  (stock: " + mat.getStockAtual() + ")");
                return this;
            }
        });
        comboMat.setPreferredSize(new Dimension(240, 28));

        JTextField campoQtdLinha   = new JTextField("1");
        campoQtdLinha.setPreferredSize(new Dimension(55, 28));
        JTextField campoPrecoLinha = new JTextField("0.00");
        campoPrecoLinha.setPreferredSize(new Dimension(80, 28));

        JButton btnAddLinha = buildSmallButton("+ Adicionar");
        JButton btnRemLinha = buildSmallButton("Remover");

        JLabel lblErroLinha = new JLabel(" ");
        lblErroLinha.setForeground(UIConstants.COLOR_DANGER);
        lblErroLinha.setFont(lblErroLinha.getFont().deriveFont(UIConstants.FONT_SMALL));

        Runnable recalcTotal = () -> {
            BigDecimal soma = BigDecimal.ZERO;
            for (Object[] row : linhasTemp) {
                int q = (Integer) row[1];
                BigDecimal p = (BigDecimal) row[2];
                soma = soma.add(p.multiply(BigDecimal.valueOf(q)));
            }
            lblTotal.setText(String.format("Total: %.2f €", soma));
        };

        btnAddLinha.addActionListener(e -> {
            try {
                if (!(comboMat.getSelectedItem() instanceof Material mat))
                    throw new IllegalArgumentException("Selecciona um material.");
                int qtd   = Integer.parseInt(campoQtdLinha.getText().trim());
                BigDecimal preco = new BigDecimal(campoPrecoLinha.getText().trim().replace(',', '.'));
                if (qtd <= 0)    throw new IllegalArgumentException("Qtd deve ser > 0.");
                if (preco.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Preço não pode ser negativo.");
                Material matFresco = materialService.buscarPorId(mat.getId());
                linhasTemp.add(new Object[]{matFresco, qtd, preco});
                modeloLinhas.addRow(new Object[]{
                        mat.getNome(), qtd, String.format("%.2f", preco)});
                recalcTotal.run();
                lblErroLinha.setText(" ");
            } catch (NumberFormatException ex) {
                lblErroLinha.setText("Qtd e preço devem ser números válidos.");
            } catch (Exception ex) {
                lblErroLinha.setText(ex.getMessage());
            }
        });

        btnRemLinha.addActionListener(e -> {
            int sel = tblLinhas.getSelectedRow();
            if (sel < 0) return;
            linhasTemp.remove(sel);
            modeloLinhas.removeRow(sel);
            recalcTotal.run();
        });

        JPanel adicionarLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        adicionarLinha.setOpaque(false);
        adicionarLinha.add(new JLabel("Material:"));
        adicionarLinha.add(comboMat);
        adicionarLinha.add(new JLabel("Qtd:"));
        adicionarLinha.add(campoQtdLinha);
        adicionarLinha.add(new JLabel("Preço/unit:"));
        adicionarLinha.add(campoPrecoLinha);
        adicionarLinha.add(btnAddLinha);
        adicionarLinha.add(btnRemLinha);

        // ── Erro global + botões ──────────────────────────────────────────
        JLabel lblErro = erroLabel();

        JButton btnGuardar  = btnPrimario("Criar Encomenda");
        JButton btnCancelar = buildButton("Cancelar");
        btnCancelar.addActionListener(ev -> dlg.dispose());

        btnGuardar.addActionListener(ev -> {
            try {
                if (!(comboForn.getSelectedItem() instanceof Fornecedor forn))
                    throw new IllegalArgumentException("Selecciona um fornecedor.");
                if (linhasTemp.isEmpty())
                    throw new IllegalArgumentException("Adiciona pelo menos um material.");

                // Criar encomenda
                Encomenda enc = new Encomenda();
                enc.setIdFornecedor(forn);
                enc.setDataPedido(LocalDate.now());
                enc.setEntregue(false);
                Encomenda encGuardada = encomendaService.guardar(enc);

                // Criar linhas e calcular total
                BigDecimal total = BigDecimal.ZERO;
                for (Object[] row : linhasTemp) {
                    Material mat = materialService.buscarPorId(((Material) row[0]).getId());
                    int      qtd  = (Integer)  row[1];
                    BigDecimal pr = (BigDecimal) row[2];
                    Linhaencomenda le = new Linhaencomenda();
                    le.setIdEncomenda(encGuardada);
                    le.setIdMaterial(mat);
                    le.setQuantidade(qtd);
                    le.setPrecoCustoUnit(pr);
                    linhaencomendaService.guardar(le);
                    total = total.add(pr.multiply(BigDecimal.valueOf(qtd)));
                }

                // Actualizar total na encomenda
                encGuardada.setValorTotalCompra(total);
                encomendaService.guardar(encGuardada);

                dlg.dispose();
                carregarEncomendas();
                JOptionPane.showMessageDialog(this, "Encomenda criada com sucesso.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        // ── Montar layout ─────────────────────────────────────────────────────
        JPanel linhaInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linhaInputs.setOpaque(false);
        linhaInputs.add(new JLabel("Material:"));
        linhaInputs.add(comboMat);
        linhaInputs.add(new JLabel("Qtd:"));
        linhaInputs.add(campoQtdLinha);
        linhaInputs.add(new JLabel("Preço/unit:"));
        linhaInputs.add(campoPrecoLinha);

        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linhaBotoes.setOpaque(false);
        linhaBotoes.add(btnAddLinha);
        linhaBotoes.add(btnRemLinha);
        linhaBotoes.add(lblErroLinha);

        JPanel secaoAdicionar = new JPanel();
        secaoAdicionar.setLayout(new BoxLayout(secaoAdicionar, BoxLayout.Y_AXIS));
        secaoAdicionar.setOpaque(false);
        secaoAdicionar.setBorder(new EmptyBorder(8, 0, 4, 0));

        JLabel lblTituloLinhas = new JLabel("Materiais da encomenda");
        lblTituloLinhas.setFont(lblTituloLinhas.getFont().deriveFont(Font.BOLD, 12f));
        lblTituloLinhas.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaInputs.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);

        secaoAdicionar.add(lblTituloLinhas);
        secaoAdicionar.add(Box.createVerticalStrut(6));
        secaoAdicionar.add(linhaInputs);
        secaoAdicionar.add(linhaBotoes);
        secaoAdicionar.add(Box.createVerticalStrut(4));
        secaoAdicionar.add(lblTotal);

        JPanel painelCentro = new JPanel(new BorderLayout(0, 6));
        painelCentro.setOpaque(false);
        painelCentro.add(secaoAdicionar, BorderLayout.NORTH);
        painelCentro.add(scrollLinhas,   BorderLayout.CENTER);

        JPanel rodapeEnc = new JPanel(new BorderLayout());
        rodapeEnc.setOpaque(false);
        rodapeEnc.setBorder(new EmptyBorder(8, 0, 0, 0));
        rodapeEnc.add(lblErro, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(btnCancelar);
        btns.add(btnGuardar);
        rodapeEnc.add(btns, BorderLayout.EAST);

        content.add(topForm,      BorderLayout.NORTH);
        content.add(painelCentro, BorderLayout.CENTER);
        content.add(rodapeEnc,    BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    /** Ver linhas de uma encomenda existente. */
    private void abrirDetalheEncomenda() {
        Encomenda enc = encomendaSelecionada();
        if (enc == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma encomenda primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = criarDialogo("Detalhe — Encomenda #" + enc.getId());
        dlg.setSize(560, 400);
        dlg.setLocationRelativeTo(this);

        JPanel content = painelConteudo();

        // ── Info cabeçalho ────────────────────────────────────────────────
        String forn   = enc.getIdFornecedor() != null ? enc.getIdFornecedor().getNome() : "—";
        String data   = enc.getDataPedido() != null ? enc.getDataPedido().format(FMT) : "—";
        String estado = Boolean.TRUE.equals(enc.getEntregue()) ? "Entregue" : "Pendente";
        String total  = enc.getValorTotalCompra() != null
                ? String.format("%.2f €", enc.getValorTotalCompra()) : "0.00 €";

        JPanel info = new JPanel(new GridLayout(2, 2, 12, 4));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(0, 0, 12, 0));
        info.add(infoLabel("Fornecedor:", forn));
        info.add(infoLabel("Estado:",     estado));
        info.add(infoLabel("Data:",       data));
        info.add(infoLabel("Total:",      total));

        // ── Tabela de linhas ──────────────────────────────────────────────
        List<Linhaencomenda> linhas = linhaencomendaService.buscarPorEncomenda(enc);
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Material", "Qtd", "Preço Unit. (€)", "Subtotal (€)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Linhaencomenda l : linhas) {
            String mat   = l.getIdMaterial() != null ? l.getIdMaterial().getNome() : "—";
            int    qtd   = l.getQuantidade() != null ? l.getQuantidade() : 0;
            BigDecimal pr = l.getPrecoCustoUnit() != null ? l.getPrecoCustoUnit() : BigDecimal.ZERO;
            BigDecimal sub = pr.multiply(BigDecimal.valueOf(qtd));
            m.addRow(new Object[]{mat, qtd,
                    String.format("%.2f", pr),
                    String.format("%.2f", sub)});
        }
        JTable t = new JTable(m);
        t.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.getColumnModel().getColumn(0).setPreferredWidth(230);
        t.getColumnModel().getColumn(1).setPreferredWidth(60);
        t.getColumnModel().getColumn(2).setPreferredWidth(110);
        t.getColumnModel().getColumn(3).setPreferredWidth(110);

        JButton btnFechar = buildButton("Fechar");
        btnFechar.addActionListener(e -> dlg.dispose());
        JPanel rod = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rod.setOpaque(false);
        rod.add(btnFechar);

        content.add(info,               BorderLayout.NORTH);
        content.add(buildTablePane(t),  BorderLayout.CENTER);
        content.add(rod,                BorderLayout.SOUTH);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void marcarEntregue() {
        Encomenda enc = encomendaSelecionada();
        if (enc == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma encomenda primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Boolean.TRUE.equals(enc.getEntregue())) {
            JOptionPane.showMessageDialog(this, "Esta encomenda já foi marcada como entregue.",
                    "Já entregue", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Marcar encomenda #" + enc.getId() + " como entregue?\n" +
                        "O stock de todos os materiais será actualizado automaticamente.",
                "Confirmar entrega", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            encomendaService.marcarComoEntregue(enc.getId());
            carregarEncomendas();
            carregarMateriais();
            JOptionPane.showMessageDialog(this,
                    "Encomenda marcada como entregue. Stock actualizado.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void eliminarEncomenda() {
        Encomenda enc = encomendaSelecionada();
        if (enc == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma encomenda primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Eliminar a encomenda #" + enc.getId() + "?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        try {
            encomendaService.eliminar(enc.getId());
            carregarEncomendas();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    // =========================================================================
    //  RENDERERS
    // =========================================================================

    /** Cor verde / amarelo / vermelho conforme o nível de stock. */
    private class StockCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (!sel && v instanceof Integer stock) {
                if (stock == 0) {
                    lbl.setForeground(UIConstants.COLOR_DANGER);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else if (stock < LIMIAR_STOCK_BAIXO) {
                    lbl.setForeground(UIConstants.COLOR_WARNING);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setForeground(UIConstants.COLOR_SUCCESS);
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
            }
            return lbl;
        }
    }

    /** Badge Pendente / Entregue nas encomendas. */
    private static class EstadoEncCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
            if (!sel && v != null) {
                boolean entregue = "Entregue".equals(v.toString());
                lbl.setBackground(entregue ? new Color(220, 252, 231) : new Color(254, 243, 199));
                lbl.setForeground(entregue ? new Color(21, 128, 61)   : new Color(146, 64, 14));
            }
            return lbl;
        }
    }

    // =========================================================================
    //  HELPERS DE LAYOUT (padrão do projecto)
    // =========================================================================

    /** Adiciona label + campo ao GridBagLayout com row*2 / row*2+1. */
    private void adicionarCampo(JPanel panel, int row, String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2;
        c.insets = new Insets(8, 0, 2, 0);
        panel.add(lbl, c);

        c.gridy = row * 2 + 1; c.insets = new Insets(0, 0, 4, 0);
        panel.add(campo, c);
    }

    private void adicionarErro(JPanel panel, int row, JLabel lblErro) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row * 2; c.gridx = 0; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 0, 0);
        panel.add(lblErro, c);
    }

    private JPanel rodape(JDialog dlg, JButton btnCancelar, JButton btnPrimario) {
        btnCancelar.addActionListener(e -> dlg.dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));
        p.add(btnCancelar);
        p.add(btnPrimario);
        return p;
    }

    private JPanel painelConteudo() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(new EmptyBorder(20, 24, 16, 24));
        p.setBackground(UIManager.getColor("Panel.background"));
        return p;
    }

    private JButton btnPrimario(String texto) {
        JButton b = buildButton(texto);
        b.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    private JLabel erroLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(UIConstants.COLOR_DANGER);
        l.setFont(l.getFont().deriveFont(UIConstants.FONT_SMALL));
        return l;
    }

    private JLabel contadorLabel() {
        JLabel l = new JLabel("");
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setForeground(UIManager.getColor("Label.disabledForeground"));
        return l;
    }

    private JDialog criarDialogo(String titulo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, titulo, true)
                : new JDialog((Dialog) owner, titulo, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);
        return dlg;
    }

    private JComboBox<Fornecedor> comboFornecedor(List<Fornecedor> lista) {
        JComboBox<Fornecedor> cb = new JComboBox<>(lista.toArray(new Fornecedor[0]));
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Fornecedor fn) setText(fn.getNome() + " (NIF: " + fn.getNif() + ")");
                return this;
            }
        });
        return cb;
    }

    private JPanel infoLabel(String label, String valor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        JLabel val = new JLabel(valor);
        val.setFont(val.getFont().deriveFont(13f));
        p.add(lbl); p.add(val);
        return p;
    }

    private void ocultarColuna(JTable table, int col) {
        table.getColumnModel().getColumn(col).setMinWidth(0);
        table.getColumnModel().getColumn(col).setMaxWidth(0);
        table.getColumnModel().getColumn(col).setWidth(0);
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private javax.swing.event.DocumentListener docListener(Runnable action) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { action.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { action.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        };
    }

    private java.awt.event.MouseAdapter dblClick(Runnable action) {
        return new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) action.run();
            }
        };
    }
}
