package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EquipasPanel extends BasePanel {

    // ── Serviços ──────────────────────────────────────────────────────────────
    private final EquipaService             equipaService;
    private final EquipafuncionarioService  equipafuncionarioService;
    private final ObraService               obraService;
    private final FuncionarioService        funcionarioService;
    private final TarefaService             tarefaService;

    // ── UI ────────────────────────────────────────────────────────────────────
    private JTextField        campoPesquisa;
    private JComboBox<String> filtroEstado;
    private JLabel            lblContador;
    private DefaultTableModel modelo;
    private JTable            tabela;

    private List<Equipa> equipasCarregadas;

    private static final String[] COLUNAS = {"ID", "Nome", "Obra", "Membros", "Estado"};

    // ─────────────────────────────────────────────────────────────────────────

    public EquipasPanel(EquipaService equipaService,
                        EquipafuncionarioService equipafuncionarioService,
                        ObraService obraService,
                        FuncionarioService funcionarioService,
                        TarefaService tarefaService) {
        this.equipaService            = equipaService;
        this.equipafuncionarioService = equipafuncionarioService;
        this.obraService              = obraService;
        this.funcionarioService       = funcionarioService;
        this.tarefaService            = tarefaService;

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);

        carregar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CABEÇALHO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JButton btnNova = buildButton("+ Nova Equipa");
        btnNova.putClientProperty("JButton.buttonType", "roundRect");
        btnNova.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNova.setForeground(Color.WHITE);
        btnNova.setFocusPainted(false);
        btnNova.addActionListener(e -> abrirDialogoNova());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(btnNova);

        return buildHeader("Equipas",
                "RF11 — criar, remover e actualizar equipas · associar a obras",
                acoes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CORPO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 12));
        corpo.setOpaque(false);
        corpo.add(buildSurface(buildBarraFiltros(), new Insets(10, 12, 10, 12)), BorderLayout.NORTH);
        corpo.add(buildAreaTabela(),   BorderLayout.CENTER);
        corpo.add(buildSurface(buildBarraAcoes(), new Insets(10, 12, 10, 12)), BorderLayout.SOUTH);
        return corpo;
    }

    // ── Barra de filtros ──────────────────────────────────────────────────────

    private JPanel buildBarraFiltros() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por nome ou obra…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        filtroEstado = new JComboBox<>(new String[]{"Todas", "Ativas", "Inativas"});
        filtroEstado.setPreferredSize(new Dimension(130, 30));
        filtroEstado.addActionListener(e -> aplicarFiltro());

        lblContador = new JLabel("0 equipas");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.PLAIN, 12f));
        lblContador.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esquerda.setOpaque(false);
        esquerda.add(campoPesquisa);
        esquerda.add(filtroEstado);
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
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180); // Nome
        tabela.getColumnModel().getColumn(2).setPreferredWidth(260); // Obra
        tabela.getColumnModel().getColumn(3).setPreferredWidth(220); // Membros
        tabela.getColumnModel().getColumn(4).setPreferredWidth(80);  // Estado

        // Badge de estado
        tabela.getColumnModel().getColumn(4).setCellRenderer(new EstadoCellRenderer());

        // Double-click → detalhe
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDetalhe();
            }
        });

        return buildTablePane(tabela);
    }

    // ── Barra de acções ───────────────────────────────────────────────────────

    private JPanel buildBarraAcoes() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);

        JButton btnDetalhe  = buildSmallButton("Ver Detalhe");
        JButton btnMembros  = buildSmallButton("Gerir Membros");
        JButton btnToggle   = buildSmallButton("Ativar/Desativar");
        JButton btnEliminar = buildSmallButton("Eliminar");

        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnDetalhe .addActionListener(e -> abrirDetalhe());
        btnMembros .addActionListener(e -> abrirGestaoMembros());
        btnToggle  .addActionListener(e -> toggleAtiva());
        btnEliminar.addActionListener(e -> eliminarSelecionada());

        barra.add(btnDetalhe);
        barra.add(btnMembros);
        barra.add(btnToggle);
        barra.add(Box.createHorizontalStrut(12));
        barra.add(btnEliminar);
        JLabel hint = new JLabel("Duplo clique para abrir detalhe da equipa");
        hint.setFont(hint.getFont().deriveFont(UIConstants.FONT_SMALL));
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        barra.add(Box.createHorizontalStrut(10));
        barra.add(hint);

        return barra;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DADOS
    // ─────────────────────────────────────────────────────────────────────────

    private void carregar() {
        equipasCarregadas = equipaService.listarTodos();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (equipasCarregadas == null) return;

        String pesquisa  = campoPesquisa  != null ? campoPesquisa.getText().trim().toLowerCase() : "";
        String estadoSel = filtroEstado   != null ? (String) filtroEstado.getSelectedItem() : "Todas";

        modelo.setRowCount(0);
        int count = 0;

        for (Equipa eq : equipasCarregadas) {
            // Filtro estado
            if ("Ativas".equals(estadoSel)   && !Boolean.TRUE.equals(eq.getAtiva()))  continue;
            if ("Inativas".equals(estadoSel) && Boolean.TRUE.equals(eq.getAtiva()))   continue;

            // Filtro pesquisa
            if (!pesquisa.isEmpty()) {
                String nome = eq.getNomeEquipa() != null ? eq.getNomeEquipa().toLowerCase() : "";
                String obra = obraLabel(eq.getIdObra()).toLowerCase();
                if (!nome.contains(pesquisa) && !obra.contains(pesquisa)) continue;
            }

            String membros = membrosLabel(eq);
            String estado  = Boolean.TRUE.equals(eq.getAtiva()) ? "Ativa" : "Inativa";

            modelo.addRow(new Object[]{eq.getId(), eq.getNomeEquipa(), obraLabel(eq.getIdObra()), membros, estado});
            count++;
        }

        if (lblContador != null)
            lblContador.setText(count + (count == 1 ? " equipa" : " equipas"));
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    private Equipa equipaSelecionada() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona uma equipa primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        try { return equipaService.buscarPorId(id); }
        catch (Exception ex) { mostrarErro(null, ex.getMessage()); return null; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — NOVA EQUIPA  (UC9)
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDialogoNova() {
        JDialog dlg = criarDialogo("Nova Equipa");
        dlg.setSize(480, 320);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBorder(new EmptyBorder(24, 28, 20, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // Nome
        JTextField campoNome = new JTextField();

        List<Obra> obras = obraService.listarTodos();
        JComboBox<Obra> comboObra = new JComboBox<>(obras.toArray(new Obra[0]));
        comboObra.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v instanceof Obra ? obraLabel((Obra) v) : "—");
                return this;
            }
        });

        JLabel lblErro = new JLabel(" ");
        lblErro.setForeground(UIConstants.COLOR_DANGER);
        lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));

        adicionarCampoForm(form, c, 0, "Nome da equipa *", campoNome);
        adicionarCampoForm(form, c, 1, "Obra associada *",  comboObra);

        c.gridy = 4; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(4, 0, 0, 0);
        form.add(lblErro, c);

        JButton btnCancelar = buildButton("Cancelar");
        JButton btnCriar    = buildButton("Criar Equipa");
        btnCriar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnCriar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> dlg.dispose());
        btnCriar.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isBlank()) { lblErro.setText("O nome é obrigatório."); return; }
            if (!(comboObra.getSelectedItem() instanceof Obra)) { lblErro.setText("Selecciona uma obra."); return; }

            try {
                Equipa nova = new Equipa();
                nova.setNomeEquipa(nome);
                nova.setIdObra((Obra) comboObra.getSelectedItem());
                nova.setAtiva(true);
                equipaService.guardar(nova);
                dlg.dispose();
                carregar();
                mostrarSucesso(null, "Equipa criada com sucesso.");
            } catch (Exception ex) {
                lblErro.setText(ex.getMessage());
            }
        });

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(14, 0, 0, 0));
        rodape.add(btnCancelar);
        rodape.add(btnCriar);

        content.add(form,   BorderLayout.CENTER);
        content.add(rodape, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DETALHE  (membros + tarefas da obra)
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDetalhe() {
        Equipa eq = equipaSelecionada();
        if (eq == null) return;

        JDialog dlg = criarDialogo("Equipa — " + eq.getNomeEquipa());
        dlg.setSize(600, 520);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIManager.getColor("Panel.background"));

        // Topo: nome + badge estado
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);
        topo.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblNome = new JLabel(eq.getNomeEquipa());
        lblNome.setFont(lblNome.getFont().deriveFont(Font.BOLD, 17f));

        JLabel badge = buildBadgeAtiva(Boolean.TRUE.equals(eq.getAtiva()));
        topo.add(lblNome, BorderLayout.WEST);
        topo.add(badge,   BorderLayout.EAST);

        // Tabs: Membros | Tarefas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(12f));
        tabs.addTab("Membros",  buildTabMembros(eq));
        tabs.addTab("Tarefas da Obra", buildTabTarefas(eq));

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton btnMembros = buildSmallButton("Gerir Membros");
        JButton btnToggle  = buildSmallButton(Boolean.TRUE.equals(eq.getAtiva()) ? "⏸ Desativar" : "▶ Ativar");
        JButton btnFechar  = buildSmallButton("Fechar");

        btnMembros.addActionListener(ev -> { dlg.dispose(); abrirGestaoMembros(); });
        btnToggle .addActionListener(ev -> { dlg.dispose(); toggleAtiva(); });
        btnFechar .addActionListener(ev -> dlg.dispose());

        rodape.add(btnMembros);
        rodape.add(btnToggle);
        rodape.add(Box.createHorizontalStrut(8));
        rodape.add(btnFechar);

        content.add(topo,   BorderLayout.NORTH);
        content.add(tabs,   BorderLayout.CENTER);
        content.add(rodape, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private JPanel buildTabMembros(Equipa eq) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 4, 4, 4));

        List<Equipafuncionario> membros = equipafuncionarioService.buscarPorEquipa(eq);

        JLabel lblTot = new JLabel(membros.size() + " membro(s) na equipa  •  Obra: " + obraLabel(eq.getIdObra()));
        lblTot.setFont(lblTot.getFont().deriveFont(Font.PLAIN, 11f));
        lblTot.setForeground(UIManager.getColor("Label.disabledForeground"));

        if (membros.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum funcionário associado a esta equipa.");
            vazio.setForeground(UIManager.getColor("Label.disabledForeground"));
            vazio.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblTot,  BorderLayout.NORTH);
            panel.add(vazio,   BorderLayout.CENTER);
            return panel;
        }

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Nome", "Cargo", "Email"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Equipafuncionario ef : membros) {
            Funcionario f = ef.getIdFuncionario();
            String cargo  = f.getIdCargo() != null ? f.getIdCargo().getNome() : "—";
            m.addRow(new Object[]{f.getNome(), cargo, f.getEmail()});
        }

        JTable t = new JTable(m);
        t.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.getColumnModel().getColumn(0).setPreferredWidth(180);
        t.getColumnModel().getColumn(1).setPreferredWidth(120);
        t.getColumnModel().getColumn(2).setPreferredWidth(200);

        panel.add(lblTot,            BorderLayout.NORTH);
        panel.add(buildTablePane(t), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTabTarefas(Equipa eq) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 4, 4, 4));

        if (eq.getIdObra() == null) {
            panel.add(buildVazio("Esta equipa não tem obra associada."), BorderLayout.CENTER);
            return panel;
        }

        List<Tarefa> tarefas = tarefaService.buscarPorObra(eq.getIdObra());

        JLabel lblTot = new JLabel(tarefas.size() + " tarefa(s) na obra desta equipa");
        lblTot.setFont(lblTot.getFont().deriveFont(Font.PLAIN, 11f));
        lblTot.setForeground(UIManager.getColor("Label.disabledForeground"));

        if (tarefas.isEmpty()) {
            panel.add(lblTot, BorderLayout.NORTH);
            panel.add(buildVazio("Nenhuma tarefa criada para esta obra."), BorderLayout.CENTER);
            return panel;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Funcionário", "Descrição", "Data Limite", "Estado"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Tarefa t : tarefas) {
            String func   = t.getIdFuncionario() != null ? t.getIdFuncionario().getNome() : "—";
            String desc   = excerto(t.getDescricao(), 45);
            String limite = t.getDataLimite() != null ? t.getDataLimite().format(fmt) : "—";
            String estado = t.getIdEstadoTarefa() != null ? t.getIdEstadoTarefa().getNomeEstado() : "—";
            m.addRow(new Object[]{func, desc, limite, estado});
        }

        JTable tbl = new JTable(m);
        tbl.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tbl.setShowVerticalLines(false);
        tbl.setFillsViewportHeight(true);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(150);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(220);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(100);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(3).setCellRenderer(new EstadoTarefaCellRenderer());

        panel.add(lblTot,               BorderLayout.NORTH);
        panel.add(buildTablePane(tbl),   BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GERIR MEMBROS — adicionar / remover funcionários
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirGestaoMembros() {
        Equipa eq = equipaSelecionada();
        if (eq == null) return;

        JDialog dlg = criarDialogo("Gerir Membros — " + eq.getNomeEquipa());
        dlg.setSize(560, 460);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIManager.getColor("Panel.background"));

        // ── Membros actuais ─────────────────────────────────────────────────
        JLabel lblActuais = new JLabel("Membros actuais");
        lblActuais.setFont(lblActuais.getFont().deriveFont(Font.BOLD, 13f));

        DefaultListModel<String> modeloActuais = new DefaultListModel<>();
        List<Equipafuncionario> membros = equipafuncionarioService.buscarPorEquipa(eq);
        for (Equipafuncionario ef : membros) {
            modeloActuais.addElement(ef.getIdFuncionario().getId()
                    + " | " + ef.getIdFuncionario().getNome()
                    + " (" + (ef.getIdFuncionario().getIdCargo() != null
                    ? ef.getIdFuncionario().getIdCargo().getNome() : "—") + ")");
        }

        JList<String> listaActuais = new JList<>(modeloActuais);
        listaActuais.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollActuais = new JScrollPane(listaActuais);
        scrollActuais.setPreferredSize(new Dimension(0, 130));

        JButton btnRemover = buildSmallButton("Remover seleccionado");
        btnRemover.setForeground(UIConstants.COLOR_DANGER);
        btnRemover.addActionListener(e -> {
            int idx = listaActuais.getSelectedIndex();
            if (idx < 0) return;
            Equipafuncionario ef = membros.get(idx);
            EquipafuncionarioId eid = new EquipafuncionarioId();
            eid.setIdEquipa(eq.getId());
            eid.setIdFuncionario(ef.getIdFuncionario().getId());
            equipafuncionarioService.eliminar(eid);
            membros.remove(idx);
            modeloActuais.remove(idx);
        });

        // ── Adicionar novo membro ───────────────────────────────────────────
        JLabel lblAdicionar = new JLabel("Adicionar funcionário");
        lblAdicionar.setFont(lblAdicionar.getFont().deriveFont(Font.BOLD, 13f));
        lblAdicionar.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Funcionários ainda não na equipa
        List<Integer> idsActuais = membros.stream()
                .map(ef -> ef.getIdFuncionario().getId())
                .collect(Collectors.toList());

        List<Funcionario> disponiveis = funcionarioService.listarTodos().stream()
                .filter(f -> !idsActuais.contains(f.getId()))
                .collect(Collectors.toList());

        JComboBox<Funcionario> comboFunc = new JComboBox<>(disponiveis.toArray(new Funcionario[0]));
        comboFunc.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Funcionario fn)
                    setText(fn.getNome() + (fn.getIdCargo() != null ? " (" + fn.getIdCargo().getNome() + ")" : ""));
                return this;
            }
        });

        JLabel lblErroAdd = new JLabel(" ");
        lblErroAdd.setForeground(UIConstants.COLOR_DANGER);
        lblErroAdd.setFont(lblErroAdd.getFont().deriveFont(UIConstants.FONT_SMALL));

        JButton btnAdicionar = buildButton("Adicionar");
        btnAdicionar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.addActionListener(e -> {
            if (!(comboFunc.getSelectedItem() instanceof Funcionario func)) {
                lblErroAdd.setText("Selecciona um funcionário.");
                return;
            }
            try {
                EquipafuncionarioId eid = new EquipafuncionarioId();
                eid.setIdEquipa(eq.getId());
                eid.setIdFuncionario(func.getId());

                Equipafuncionario ef = new Equipafuncionario();
                ef.setId(eid);
                ef.setIdEquipa(eq);
                ef.setIdFuncionario(func);
                equipafuncionarioService.guardar(ef);

                // Actualizar listas
                membros.add(ef);
                modeloActuais.addElement(func.getId() + " | " + func.getNome()
                        + " (" + (func.getIdCargo() != null ? func.getIdCargo().getNome() : "—") + ")");
                comboFunc.removeItem(func);
                lblErroAdd.setText(" ");
            } catch (Exception ex) {
                lblErroAdd.setText(ex.getMessage());
                ex.printStackTrace();
            }
        });

        comboFunc.setPreferredSize(new Dimension(360, 34));
        btnAdicionar.setPreferredSize(new Dimension(120, 34));

        JPanel addRow = new JPanel(new GridBagLayout());
        addRow.setOpaque(false);
        GridBagConstraints addC = new GridBagConstraints();
        addC.gridx = 0;
        addC.gridy = 0;
        addC.weightx = 1.0;
        addC.fill = GridBagConstraints.HORIZONTAL;
        addC.insets = new Insets(0, 0, 0, 8);
        addRow.add(comboFunc, addC);
        addC.gridx = 1;
        addC.weightx = 0;
        addC.fill = GridBagConstraints.NONE;
        addC.insets = new Insets(0, 0, 0, 0);
        addRow.add(btnAdicionar, addC);

        // ── Montar painel ───────────────────────────────────────────────────
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setOpaque(false);

        corpo.add(lblActuais);
        corpo.add(Box.createVerticalStrut(6));
        corpo.add(scrollActuais);
        corpo.add(Box.createVerticalStrut(4));
        JPanel remPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        remPanel.setOpaque(false);
        remPanel.add(btnRemover);
        corpo.add(remPanel);
        corpo.add(lblAdicionar);
        corpo.add(Box.createVerticalStrut(6));
        corpo.add(addRow);
        corpo.add(Box.createVerticalStrut(2));
        corpo.add(lblErroAdd);

        JButton btnFechar = buildButton("Fechar");
        btnFechar.addActionListener(e -> { dlg.dispose(); carregar(); });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.setOpaque(false);
        rodape.add(btnFechar);

        content.add(corpo,  BorderLayout.CENTER);
        content.add(rodape, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ATIVAR / DESATIVAR
    // ─────────────────────────────────────────────────────────────────────────

    private void toggleAtiva() {
        Equipa eq = equipaSelecionada();
        if (eq == null) return;

        boolean novoEstado = !Boolean.TRUE.equals(eq.getAtiva());
        String acao = novoEstado ? "ativar" : "desativar";

        int conf = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes " + acao + " a equipa \"" + eq.getNomeEquipa() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (conf != JOptionPane.YES_OPTION) return;

        try {
            eq.setAtiva(novoEstado);
            equipaService.guardar(eq);
            carregar();
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ELIMINAR  (UC12)
    // ─────────────────────────────────────────────────────────────────────────

    public void eliminarSelecionada() {
        Equipa eq = equipaSelecionada();
        if (eq == null) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar a equipa \"" + eq.getNomeEquipa() + "\"?\n"
                        + "Todos os membros serão desassociados. Esta acção não pode ser desfeita.",
                "Confirmar eliminação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (conf != JOptionPane.YES_OPTION) return;

        try {
            equipaService.eliminar(eq.getId());
            carregar();
        } catch (Exception ex) {
            mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
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
        JOptionPane.showMessageDialog(parent != null ? parent : this,
                msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private static String obraLabel(Obra obra) {
        if (obra == null) return "—";
        return obra.getRua() + ", nº " + obra.getNporta() + " — " + obra.getLocalidade();
    }

    private String membrosLabel(Equipa eq) {
        try {
            List<Equipafuncionario> lista = equipafuncionarioService.buscarPorEquipa(eq);
            if (lista.isEmpty()) return "Sem membros";
            return lista.stream()
                    .map(ef -> ef.getIdFuncionario().getNome())
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "—";
        }
    }

    private static String excerto(String texto, int max) {
        if (texto == null || texto.isBlank()) return "—";
        String s = texto.replace('\n', ' ').trim();
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private JLabel buildVazio(String msg) {
        JLabel lbl = new JLabel(msg);
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    /** Badge circular ativa / inativa */
    private JLabel buildBadgeAtiva(boolean ativa) {
        Color cor = ativa ? UIConstants.COLOR_SUCCESS : new Color(100, 116, 139);
        String texto = ativa ? "Ativa" : "Inativa";
        JLabel badge = new JLabel(texto) {
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

    /** Helper para adicionar label + campo no formulário (pattern do projeto) */
    private void adicionarCampoForm(JPanel panel, GridBagConstraints c,
                                    int row, String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));

        c.gridy = row * 2;
        c.gridx = 0; c.gridwidth = 2;
        c.insets = new Insets(8, 0, 2, 0);
        panel.add(lbl, c);

        c.gridy = row * 2 + 1;
        c.gridx = 0; c.gridwidth = 2;
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(campo, c);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RENDERERS
    // ─────────────────────────────────────────────────────────────────────────

    private static class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
            if (!sel && v != null) {
                boolean ativa = "Ativa".equals(v.toString());
                lbl.setBackground(ativa ? new Color(220, 252, 231) : new Color(241, 245, 249));
                lbl.setForeground(ativa ? new Color(21, 128, 61)   : new Color(100, 116, 139));
            }
            return lbl;
        }
    }

    private static class EstadoTarefaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
            if (!sel && v != null) {
                String s = v.toString().toLowerCase();
                Color bg, fg;
                if (s.contains("conclu"))      { bg = new Color(220,252,231); fg = new Color(21,128,61); }
                else if (s.contains("execu"))  { bg = new Color(219,234,254); fg = new Color(29,78,216); }
                else if (s.contains("cancel")) { bg = new Color(254,226,226); fg = new Color(185,28,28); }
                else if (s.contains("bloque")) { bg = new Color(254,243,199); fg = new Color(146,64,14); }
                else                           { bg = new Color(241,245,249); fg = new Color(100,116,139); }
                lbl.setBackground(bg);
                lbl.setForeground(fg);
            }
            return lbl;
        }
    }
}
