package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Painel de Gestão de Obras — RF02, RF07, RF08, RF22
 *
 * Funcionalidades:
 *  • Listar obras com filtro por estado e pesquisa por descrição/cliente  (RF22)
 *  • Criar nova obra associada a cliente                                   (RF02)
 *  • Ver detalhe da obra (info, endereço, datas)
 *  • Alterar estado da obra                                                (RF07)
 *  • Marcar obra como concluída                                            (RF08)
 *  • Arquivar / Cancelar obra
 *  • Agendar visita à obra                                                 (RF03)
 *  • Ver histórico de obras de um cliente                                  (RF21)
 */
public class ObrasPanel extends BasePanel {

    // ── Serviços ──────────────────────────────────────────────────────────
    private final ObraService         obraService;
    private final ClienteService      clienteService;
    private final EstadoobraService   estadoobraService;
    private final VisitaService       visitaService;
    private final OrcamentoService    orcamentoService;
    private final CodpostalService    codpostalService;

    // ── Componentes da tabela ──────────────────────────────────────────────
    private DefaultTableModel modeloTabela;
    private JTable            tabela;
    private JTextField        campoPesquisa;
    private JComboBox<String> filtroEstado;
    private JLabel            lblContador;

    // Estado de dados carregados
    private List<Obra> obrasCarregadas;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────

    public ObrasPanel(ObraService obraService, ClienteService clienteService,
                      EstadoobraService estadoobraService, VisitaService visitaService,
                      OrcamentoService orcamentoService, CodpostalService codpostalService) {
        this.obraService       = obraService;
        this.clienteService    = clienteService;
        this.estadoobraService = estadoobraService;
        this.visitaService     = visitaService;
        this.orcamentoService  = orcamentoService;
        this.codpostalService  = codpostalService;

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);
        carregar();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CABEÇALHO
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JButton btnNova = buildButton("+ Nova Obra");
        btnNova.setBackground(UIConstants.COLOR_INFO);
        btnNova.setForeground(Color.WHITE);
        btnNova.setOpaque(true);
        btnNova.setBorderPainted(false);
        btnNova.addActionListener(e -> abrirDialogoNovaObra());

        JButton btnRefresh = buildButton("↻");
        btnRefresh.setToolTipText("Actualizar lista");
        btnRefresh.addActionListener(e -> carregar());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(btnRefresh);
        acoes.add(btnNova);

        return buildHeader("Obras", "Gestão e acompanhamento de obras  ·  RF02 · RF07 · RF08 · RF22", acoes);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CORPO PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 14));
        corpo.setOpaque(false);
        corpo.add(buildBarraFiltros(), BorderLayout.NORTH);
        corpo.add(buildTabela(), BorderLayout.CENTER);
        corpo.add(buildBarraAcoes(), BorderLayout.SOUTH);
        return corpo;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  BARRA DE FILTROS
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildBarraFiltros() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        // Pesquisa
        campoPesquisa = buildSearchField("Pesquisar por obra ou cliente…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        // Filtro de estado
        filtroEstado = new JComboBox<>(new String[]{"Todos os estados", "Planeada", "Em execução", "Concluída", "Arquivada", "Cancelada"});
        filtroEstado.setPreferredSize(new Dimension(170, 30));
        filtroEstado.addActionListener(e -> aplicarFiltro());

        // Contador
        lblContador = new JLabel("0 obras");
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

    // ─────────────────────────────────────────────────────────────────────
    //  TABELA
    // ─────────────────────────────────────────────────────────────────────

    private JScrollPane buildTabela() {
        modeloTabela = new DefaultTableModel(
                new String[]{"#", "Descrição / Obra", "Cliente", "Localidade", "Data Criação", "Estado"},
                0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return String.class; }
        };

        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setFont(tabela.getFont().deriveFont(13f));
        tabela.getTableHeader().setFont(tabela.getFont().deriveFont(Font.BOLD, 11f));

        // Larguras das colunas
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);   // #
        tabela.getColumnModel().getColumn(0).setMaxWidth(55);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(260);  // Descrição
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);  // Cliente
        tabela.getColumnModel().getColumn(3).setPreferredWidth(130);  // Localidade
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);  // Data
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);  // Estado

        // Renderer da coluna Estado (badge colorido)
        tabela.getColumnModel().getColumn(5).setCellRenderer(new EstadoCellRenderer());

        // Duplo clique → detalhe
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0)
                    abrirDetalheObra();
            }
        });

        JScrollPane scroll = buildTablePane(tabela);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  BARRA DE ACÇÕES (botões contextuais)
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildBarraAcoes() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);

        JButton btnDetalhe   = buildSmallButton("🔍 Ver Detalhe");
        JButton btnEstado    = buildSmallButton("⟳ Alterar Estado");
        JButton btnVisita    = buildSmallButton("📅 Agendar Visita");
        JButton btnHistorico = buildSmallButton("📋 Histórico Cliente");
        JButton btnEliminar  = buildSmallButton("🗑 Eliminar");

        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnDetalhe  .addActionListener(e -> abrirDetalheObra());
        btnEstado   .addActionListener(e -> abrirDialogoAlterarEstado());
        btnVisita   .addActionListener(e -> abrirDialogoAgendarVisita());
        btnHistorico.addActionListener(e -> abrirHistoricoCliente());
        btnEliminar .addActionListener(e -> eliminarObra());

        barra.add(btnDetalhe);
        barra.add(btnEstado);
        barra.add(btnVisita);
        barra.add(btnHistorico);
        barra.add(Box.createHorizontalStrut(16));
        barra.add(btnEliminar);

        return barra;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CARREGAR / FILTRAR DADOS
    // ─────────────────────────────────────────────────────────────────────

    private void carregar() {
        obrasCarregadas = obraService.listarTodos();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String pesquisa = campoPesquisa != null ? campoPesquisa.getText().trim().toLowerCase() : "";
        String estadoSel = filtroEstado != null ? (String) filtroEstado.getSelectedItem() : "Todos os estados";

        modeloTabela.setRowCount(0);

        List<Obra> filtradas = (obrasCarregadas == null ? List.<Obra>of() : obrasCarregadas)
                .stream()
                .filter(o -> {
                    // Filtro de estado
                    if (!"Todos os estados".equals(estadoSel)) {
                        if (o.getIdEstadoObra() == null) return false;
                        if (!o.getIdEstadoObra().getNomeEstado().equalsIgnoreCase(estadoSel)) return false;
                    }
                    // Filtro de pesquisa
                    if (!pesquisa.isEmpty()) {
                        String desc    = o.getDescricao()   != null ? o.getDescricao().toLowerCase()   : "";
                        String cliente = o.getIdCliente()   != null ? o.getIdCliente().getNome().toLowerCase() : "";
                        String loc     = o.getLocalidade()  != null ? o.getLocalidade().toLowerCase()  : "";
                        return desc.contains(pesquisa) || cliente.contains(pesquisa) || loc.contains(pesquisa);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        for (Obra o : filtradas) {
            String desc    = o.getDescricao()  != null && !o.getDescricao().isBlank()
                    ? truncar(o.getDescricao(), 50) : "#" + o.getId();
            String cliente = o.getIdCliente()  != null ? o.getIdCliente().getNome() : "—";
            String local   = o.getLocalidade() != null ? o.getLocalidade() : "—";
            String data    = o.getDataCriacao() != null ? o.getDataCriacao().format(FMT) : "—";
            String estado  = o.getIdEstadoObra() != null ? o.getIdEstadoObra().getNomeEstado() : "—";
            modeloTabela.addRow(new Object[]{o.getId(), desc, cliente, local, data, estado});
        }

        if (lblContador != null)
            lblContador.setText(filtradas.size() + (filtradas.size() == 1 ? " obra" : " obras"));
    }

    // ─────────────────────────────────────────────────────────────────────
    //  UTILITÁRIO — obra seleccionada
    // ─────────────────────────────────────────────────────────────────────

    private Obra obraSelecionada() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione uma obra da lista primeiro.",
                    "Nenhuma obra seleccionada", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        Integer id = (Integer) modeloTabela.getValueAt(row, 0);
        return obraService.buscarPorId(id);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — Nova Obra (RF02)
    // ─────────────────────────────────────────────────────────────────────

    private void abrirDialogoNovaObra() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Nova Obra", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 480);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        // Título
        JLabel titulo = new JLabel("Registar Nova Obra");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(new EmptyBorder(0, 0, 18, 0));

        // Formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // Cliente
        JComboBox<Cliente> cbCliente = new JComboBox<>();
        clienteService.listarTodos().forEach(cbCliente::addItem);
        cbCliente.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Cliente) setText(((Cliente) v).getNome());
                return this;
            }
        });

        // Campos
        JTextField tfDescricao  = new JTextField();
        JTextField tfRua        = new JTextField();
        JTextField tfNPorta     = new JTextField();
        JTextField tfLocalidade = new JTextField();

        JComboBox<Codpostal> cbCodpostal = new JComboBox<>();
        codpostalService.listarTodos().forEach(cbCodpostal::addItem);
        cbCodpostal.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Codpostal) setText(((Codpostal) v).getCodpostal());
                return this;
            }
        });

        int r = 0;
        addFormRow(form, gbc, r++, "Cliente *",     cbCliente);
        addFormRow(form, gbc, r++, "Descrição",     tfDescricao);
        addFormRow(form, gbc, r++, "Rua *",         tfRua);
        addFormRow(form, gbc, r++, "Nº Porta *",    tfNPorta);
        addFormRow(form, gbc, r++, "Localidade *",  tfLocalidade);
        addFormRow(form, gbc, r++, "Cód. Postal *", cbCodpostal);

        // Botões
        JButton btnGuardar  = buildButton("Guardar");
        JButton btnCancelar = buildButton("Cancelar");
        btnGuardar.setBackground(UIConstants.COLOR_INFO);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorderPainted(false);

        btnGuardar.addActionListener(ev -> {
            try {
                if (cbCliente.getSelectedItem() == null)
                    throw new IllegalArgumentException("Seleccione um cliente.");
                if (tfRua.getText().isBlank())
                    throw new IllegalArgumentException("A rua é obrigatória.");
                if (tfNPorta.getText().isBlank())
                    throw new IllegalArgumentException("O número de porta é obrigatório.");
                if (tfLocalidade.getText().isBlank())
                    throw new IllegalArgumentException("A localidade é obrigatória.");
                if (cbCodpostal.getSelectedItem() == null)
                    throw new IllegalArgumentException("Seleccione um código postal.");

                Obra nova = new Obra();
                nova.setIdCliente((Cliente) cbCliente.getSelectedItem());
                nova.setDescricao(tfDescricao.getText().trim());
                nova.setRua(tfRua.getText().trim());
                nova.setNporta(tfNPorta.getText().trim());
                nova.setLocalidade(tfLocalidade.getText().trim());
                nova.setIdCodpostal((Codpostal) cbCodpostal.getSelectedItem());
                nova.setDataCriacao(LocalDate.now());

                // Estado inicial: Planeada (id=1) via serviço
                List<Estadoobra> estados = estadoobraService.listarTodos();
                estados.stream()
                        .filter(e -> e.getNomeEstado().equalsIgnoreCase("Planeada"))
                        .findFirst()
                        .ifPresent(nova::setIdEstadoObra);

                obraService.guardar(nova);
                dlg.dispose();
                carregar();
                JOptionPane.showMessageDialog(this, "Obra registada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Erro ao guardar", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(ev -> dlg.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        content.add(titulo, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — Detalhe da Obra
    // ─────────────────────────────────────────────────────────────────────

    private void abrirDetalheObra() {
        Obra obra = obraSelecionada();
        if (obra == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalhe da Obra", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(540, 520);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        // Título + badge estado
        JPanel topoTitulo = new JPanel(new BorderLayout(12, 0));
        topoTitulo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Obra #" + obra.getId());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));

        String nomeEstado = obra.getIdEstadoObra() != null ? obra.getIdEstadoObra().getNomeEstado() : "—";
        JLabel badgeEstado = buildBadgeEstado(nomeEstado);

        topoTitulo.add(lblTitulo,   BorderLayout.WEST);
        topoTitulo.add(badgeEstado, BorderLayout.EAST);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Corpo com detalhes
        JPanel detalhe = new JPanel(new GridBagLayout());
        detalhe.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 16);
        gbc.anchor = GridBagConstraints.WEST;

        String cliente   = obra.getIdCliente() != null ? obra.getIdCliente().getNome() : "—";
        String nif       = obra.getIdCliente() != null && obra.getIdCliente().getNif() != null ? obra.getIdCliente().getNif() : "—";
        String desc      = obra.getDescricao() != null ? obra.getDescricao() : "Sem descrição";
        String morada    = obra.getRua() + " nº" + obra.getNporta() + ", " + obra.getLocalidade();
        String cp        = obra.getIdCodpostal() != null ? obra.getIdCodpostal().getCodpostal() : "—";
        String dataCria  = obra.getDataCriacao() != null ? obra.getDataCriacao().format(FMT) : "—";

        addDetalheRow(detalhe, gbc, 0, "Cliente",        cliente);
        addDetalheRow(detalhe, gbc, 1, "NIF Cliente",    nif);
        addDetalheRow(detalhe, gbc, 2, "Descrição",      desc);
        addDetalheRow(detalhe, gbc, 3, "Morada",         morada);
        addDetalheRow(detalhe, gbc, 4, "Código Postal",  cp);
        addDetalheRow(detalhe, gbc, 5, "Data Registo",   dataCria);
        addDetalheRow(detalhe, gbc, 6, "Estado",         nomeEstado);

        // Visitas
        List<Visita> visitas = visitaService.buscarPorObra(obra);
        addDetalheRow(detalhe, gbc, 7, "Visitas",
                visitas.isEmpty() ? "Nenhuma visita registada" : visitas.size() + " visita(s) registada(s)");

        // Orçamento
        orcamentoService.buscarPorObra(obra).ifPresentOrElse(
                orc -> addDetalheRow(detalhe, gbc, 8, "Orçamento", orc.getAprovado() ? "✓ Aprovado" : "Pendente de aprovação"),
                ()   -> addDetalheRow(detalhe, gbc, 8, "Orçamento", "Sem orçamento")
        );

        // Painel de acções rápidas
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);
        acoes.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnAlterarEstado = buildSmallButton("Alterar Estado");
        JButton btnAgendarVisita = buildSmallButton("Agendar Visita");
        JButton btnFechar        = buildSmallButton("Fechar");

        btnAlterarEstado.addActionListener(ev -> { dlg.dispose(); abrirDialogoAlterarEstado(); });
        btnAgendarVisita.addActionListener(ev -> { dlg.dispose(); abrirDialogoAgendarVisita(); });
        btnFechar       .addActionListener(ev -> dlg.dispose());

        acoes.add(btnAlterarEstado);
        acoes.add(btnAgendarVisita);
        acoes.add(Box.createHorizontalStrut(8));
        acoes.add(btnFechar);

        content.add(topoTitulo,  BorderLayout.NORTH);
        content.add(detalhe,     BorderLayout.CENTER);
        content.add(acoes,       BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — Alterar Estado (RF07 + RF08)
    // ─────────────────────────────────────────────────────────────────────

    private void abrirDialogoAlterarEstado() {
        Obra obra = obraSelecionada();
        if (obra == null) return;

        List<Estadoobra> estados = estadoobraService.listarTodos();
        if (estados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não existem estados configurados.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Alterar Estado da Obra", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 280);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        // Info
        JLabel lblInfo = new JLabel("<html><b>Obra:</b> " + truncar(obra.getDescricao() != null ? obra.getDescricao() : "#" + obra.getId(), 45)
                + "<br><b>Estado actual:</b> " + (obra.getIdEstadoObra() != null ? obra.getIdEstadoObra().getNomeEstado() : "—") + "</html>");
        lblInfo.setFont(lblInfo.getFont().deriveFont(13f));
        lblInfo.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Explicação DTE
        JLabel lblDTE = new JLabel("<html><small>Atenção: siga o fluxo Planeada → Em execução → Concluída → Arquivada</small></html>");
        lblDTE.setForeground(UIManager.getColor("Label.disabledForeground"));
        lblDTE.setFont(lblDTE.getFont().deriveFont(11f));

        // Combo de estados
        JComboBox<Estadoobra> cbEstado = new JComboBox<>();
        estados.forEach(cbEstado::addItem);
        cbEstado.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof Estadoobra) setText(((Estadoobra) v).getNomeEstado());
                return this;
            }
        });
        // Pré-seleccionar estado actual
        if (obra.getIdEstadoObra() != null) {
            for (int i = 0; i < cbEstado.getItemCount(); i++) {
                if (cbEstado.getItemAt(i).getId().equals(obra.getIdEstadoObra().getId())) {
                    cbEstado.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel centro = new JPanel(new GridLayout(4, 1, 0, 6));
        centro.setOpaque(false);
        centro.add(lblInfo);
        JLabel lblLbl = new JLabel("Novo estado:");
        lblLbl.setFont(lblLbl.getFont().deriveFont(Font.PLAIN, 12f));
        centro.add(lblLbl);
        centro.add(cbEstado);
        centro.add(lblDTE);

        JButton btnAplicar  = buildButton("Aplicar");
        JButton btnCancelar = buildButton("Cancelar");
        btnAplicar.setBackground(UIConstants.COLOR_INFO);
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setOpaque(true);
        btnAplicar.setBorderPainted(false);

        btnAplicar.addActionListener(ev -> {
            Estadoobra novoEstado = (Estadoobra) cbEstado.getSelectedItem();
            if (novoEstado == null) return;
            try {
                // Regra RF08: marcar concluída usa finalizar() que verifica faturas
                if (novoEstado.getNomeEstado().equalsIgnoreCase("Concluída")) {
                    obraService.finalizar(obra.getId());
                } else {
                    obra.setIdEstadoObra(novoEstado);
                    obraService.guardar(obra);
                }
                dlg.dispose();
                carregar();
                JOptionPane.showMessageDialog(this,
                        "Estado alterado para «" + novoEstado.getNomeEstado() + "» com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Erro ao alterar estado", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(ev -> dlg.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnCancelar);
        btnPanel.add(btnAplicar);

        content.add(centro,   BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — Agendar Visita (RF03)
    // ─────────────────────────────────────────────────────────────────────

    private void abrirDialogoAgendarVisita() {
        Obra obra = obraSelecionada();
        if (obra == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Agendar Visita", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 360);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.setBackground(UIManager.getColor("Panel.background"));

        JLabel lblTitulo = new JLabel("Agendar Visita — " + truncar(obra.getDescricao() != null ? obra.getDescricao() : "#" + obra.getId(), 38));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 15f));
        lblTitulo.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // Data — formato dd/MM/yyyy HH:mm
        JTextField tfData  = new JTextField(LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        JTextArea  taNotas = new JTextArea(3, 20);
        taNotas.setLineWrap(true);
        taNotas.setWrapStyleWord(true);
        taNotas.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor") != null
                        ? UIManager.getColor("Component.borderColor") : new Color(200, 200, 200)));

        addFormRow(form, gbc, 0, "Data e hora *\n(dd/MM/yyyy HH:mm)", tfData);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblNotas = new JLabel("Notas / Medições");
        lblNotas.setFont(lblNotas.getFont().deriveFont(Font.PLAIN, 12f));
        form.add(lblNotas, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        form.add(new JScrollPane(taNotas), gbc);

        JButton btnGuardar  = buildButton("Agendar");
        JButton btnCancelar = buildButton("Cancelar");
        btnGuardar.setBackground(UIConstants.COLOR_SUCCESS);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorderPainted(false);

        btnGuardar.addActionListener(ev -> {
            try {
                String dataStr = tfData.getText().trim();
                LocalDateTime ldt = LocalDateTime.parse(dataStr,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                Visita v = new Visita();
                v.setIdObra(obra);
                v.setDataVisita(ldt.atZone(ZoneId.systemDefault()).toInstant());
                v.setNotasMedicoes(taNotas.getText().trim().isEmpty() ? null : taNotas.getText().trim());

                visitaService.guardar(v);
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Visita agendada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dlg, "Formato de data inválido. Use dd/MM/yyyy HH:mm", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Erro ao agendar visita", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(ev -> dlg.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        content.add(lblTitulo, BorderLayout.NORTH);
        content.add(form,      BorderLayout.CENTER);
        content.add(btnPanel,  BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  DIÁLOGO — Histórico do Cliente (RF21)
    // ─────────────────────────────────────────────────────────────────────

    private void abrirHistoricoCliente() {
        Obra obra = obraSelecionada();
        if (obra == null || obra.getIdCliente() == null) return;

        Cliente cliente = obra.getIdCliente();
        List<Obra> historico = obraService.buscarPorCliente(cliente);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Histórico de Obras — " + cliente.getNome(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(600, 400);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIManager.getColor("Panel.background"));

        JLabel lblTitulo = new JLabel("Histórico de " + cliente.getNome()
                + "  (" + historico.size() + " obra" + (historico.size() != 1 ? "s" : "") + ")");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"#", "Descrição", "Localidade", "Data", "Estado"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Obra o : historico) {
            modelo.addRow(new Object[]{
                    o.getId(),
                    truncar(o.getDescricao() != null ? o.getDescricao() : "—", 44),
                    o.getLocalidade(),
                    o.getDataCriacao() != null ? o.getDataCriacao().format(FMT) : "—",
                    o.getIdEstadoObra() != null ? o.getIdEstadoObra().getNomeEstado() : "—"
            });
        }

        JTable tHistorico = new JTable(modelo);
        tHistorico.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        tHistorico.setShowVerticalLines(false);
        tHistorico.setFillsViewportHeight(true);
        tHistorico.getColumnModel().getColumn(4).setCellRenderer(new EstadoCellRenderer());

        JScrollPane scroll = buildTablePane(tHistorico);

        JButton btnFechar = buildButton("Fechar");
        btnFechar.addActionListener(ev -> dlg.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnFechar);

        content.add(lblTitulo, BorderLayout.NORTH);
        content.add(scroll,    BorderLayout.CENTER);
        content.add(btnPanel,  BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  ELIMINAR OBRA
    // ─────────────────────────────────────────────────────────────────────

    private void eliminarObra() {
        Obra obra = obraSelecionada();
        if (obra == null) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Tem a certeza que pretende eliminar a obra #" + obra.getId() + "?\n"
                        + "Esta acção não pode ser desfeita e eliminará todos os dados associados.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (conf != JOptionPane.YES_OPTION) return;

        try {
            obraService.eliminar(obra.getId());
            carregar();
            JOptionPane.showMessageDialog(this, "Obra eliminada.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro ao eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  RENDERER — badge de estado colorido
    // ─────────────────────────────────────────────────────────────────────

    private static class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(2, 10, 2, 10));
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));

            if (!sel && v != null) {
                String estado = v.toString().toLowerCase();
                Color bg, fg;
                if (estado.contains("execu")) {
                    bg = new Color(219, 234, 254); fg = new Color(29, 78, 216);
                } else if (estado.contains("conclu")) {
                    bg = new Color(220, 252, 231); fg = new Color(21, 128, 61);
                } else if (estado.contains("arquiv")) {
                    bg = new Color(241, 245, 249); fg = new Color(100, 116, 139);
                } else if (estado.contains("cancel")) {
                    bg = new Color(254, 226, 226); fg = new Color(185, 28, 28);
                } else { // Planeada / outros
                    bg = new Color(254, 243, 199); fg = new Color(146, 64, 14);
                }
                lbl.setBackground(bg);
                lbl.setForeground(fg);
            }
            return lbl;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  BADGE ESTADO (para diálogos)
    // ─────────────────────────────────────────────────────────────────────

    private JLabel buildBadgeEstado(String estado) {
        Color cor;
        String n = estado.toLowerCase();
        if (n.contains("execu"))  cor = UIConstants.COLOR_INFO;
        else if (n.contains("conclu")) cor = UIConstants.COLOR_SUCCESS;
        else if (n.contains("cancel")) cor = UIConstants.COLOR_DANGER;
        else if (n.contains("arquiv")) cor = new Color(100, 116, 139);
        else                            cor = UIConstants.COLOR_WARNING;

        JLabel badge = new JLabel(estado) {
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
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HELPERS — formulário
    // ─────────────────────────────────────────────────────────────────────

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent campo) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        lbl.setPreferredSize(new Dimension(120, 28));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        form.add(campo, gbc);
        gbc.weightx = 0;
    }

    private void addDetalheRow(JPanel panel, GridBagConstraints gbc, int row, String label, String valor) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        lbl.setPreferredSize(new Dimension(120, 24));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JLabel val = new JLabel(valor);
        val.setFont(val.getFont().deriveFont(Font.PLAIN, 13f));
        panel.add(val, gbc);
        gbc.weightx = 0;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HELPER — truncar texto
    // ─────────────────────────────────────────────────────────────────────

    private static String truncar(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}