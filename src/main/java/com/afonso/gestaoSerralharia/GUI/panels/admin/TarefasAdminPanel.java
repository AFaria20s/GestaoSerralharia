package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Estadotarefa;
import com.afonso.gestaoSerralharia.models.Equipa;
import com.afonso.gestaoSerralharia.models.Equipafuncionario;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.services.EstadotarefaService;
import com.afonso.gestaoSerralharia.services.EquipaService;
import com.afonso.gestaoSerralharia.services.EquipafuncionarioService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.TarefaService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TarefasAdminPanel extends BasePanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String FILTRO_TODAS_OBRAS = "Todas as obras";
    private static final String FILTRO_TODOS_FUNCIONARIOS = "Todos os funcionários";
    private static final String FILTRO_TODOS_ESTADOS = "Todos os estados";
    private static final String FILTRO_TODOS_PRAZOS = "Qualquer prazo";

    private final TarefaService tarefaService;
    private final ObraService obraService;
    private final FuncionarioService funcionarioService;
    private final EstadotarefaService estadotarefaService;
    private final EquipaService equipaService;
    private final EquipafuncionarioService equipafuncionarioService;

    private JTextField campoPesquisa;
    private JComboBox<ItemObra> filtroObra;
    private JComboBox<ItemFuncionario> filtroFuncionario;
    private JComboBox<ItemEstado> filtroEstado;
    private JComboBox<String> filtroPrazo;
    private DefaultTableModel modelo;
    private JTable tabela;
    private JLabel lblContador;
    private JLabel lblTotalValor;
    private JLabel lblAtrasadasValor;
    private JLabel lblHojeValor;
    private JLabel lblSemanaValor;

    private List<Tarefa> tarefasCarregadas = List.of();
    private List<Obra> obras = List.of();
    private List<Funcionario> funcionarios = List.of();
    private List<Estadotarefa> estados = List.of();
    private List<Equipa> equipas = List.of();

    public TarefasAdminPanel(TarefaService tarefaService,
                             ObraService obraService,
                             FuncionarioService funcionarioService,
                             EstadotarefaService estadotarefaService,
                             EquipaService equipaService,
                             EquipafuncionarioService equipafuncionarioService) {
        this.tarefaService = tarefaService;
        this.obraService = obraService;
        this.funcionarioService = funcionarioService;
        this.estadotarefaService = estadotarefaService;
        this.equipaService = equipaService;
        this.equipafuncionarioService = equipafuncionarioService;

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        carregar();
    }

    private JPanel buildHeader() {
        JButton btnNova = buildButton("+ Nova Tarefa");
        btnNova.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNova.setForeground(Color.WHITE);
        btnNova.setOpaque(true);
        btnNova.setBorderPainted(false);
        btnNova.addActionListener(e -> abrirDialogoNova());

        JButton btnRefresh = buildButton("↻");
        btnRefresh.setToolTipText("Actualizar tarefas");
        btnRefresh.addActionListener(e -> carregar());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(btnRefresh);
        acoes.add(btnNova);

        return buildHeader("Tarefas",
                "",
                acoes);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(buildSummaryRow(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(buildFiltersBar(), BorderLayout.NORTH);
        centro.add(buildTableArea(), BorderLayout.CENTER);
        centro.add(buildActionsBar(), BorderLayout.SOUTH);

        body.add(centro, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        lblTotalValor = new JLabel("0");
        lblAtrasadasValor = new JLabel("0");
        lblHojeValor = new JLabel("0");
        lblSemanaValor = new JLabel("0");

        row.add(buildStatCard("Total de tarefas", "Tarefas activas e históricas", lblTotalValor, UIConstants.COLOR_INFO));
        row.add(buildStatCard("Atrasadas", "Prazo ultrapassado", lblAtrasadasValor, UIConstants.COLOR_DANGER));
        row.add(buildStatCard("Vencem hoje", "Prioridade imediata", lblHojeValor, UIConstants.COLOR_WARNING));
        row.add(buildStatCard("Próximos 7 dias", "Planeamento semanal", lblSemanaValor, UIConstants.COLOR_SUCCESS));

        return row;
    }

    private JPanel buildStatCard(String titulo, String subtitulo, JLabel valor, Color cor) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 12f));

        JPanel badge = new JPanel();
        badge.setPreferredSize(new Dimension(10, 10));
        badge.setBackground(cor);
        badge.setBorder(BorderFactory.createLineBorder(cor.darker()));

        top.add(lblTitulo, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 24f));

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));
        lblSub.setFont(lblSub.getFont().deriveFont(11f));

        card.add(top, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFiltersBar() {
        JPanel barra = new JPanel(new BorderLayout(12, 8));
        barra.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por tarefa, obra ou funcionário…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });

        filtroObra = new JComboBox<>();
        filtroObra.setPreferredSize(new Dimension(230, 30));
        filtroObra.addActionListener(e -> aplicarFiltro());

        filtroFuncionario = new JComboBox<>();
        filtroFuncionario.setPreferredSize(new Dimension(220, 30));
        filtroFuncionario.addActionListener(e -> aplicarFiltro());

        filtroEstado = new JComboBox<>();
        filtroEstado.setPreferredSize(new Dimension(170, 30));
        filtroEstado.addActionListener(e -> aplicarFiltro());

        filtroPrazo = new JComboBox<>(new String[]{
                FILTRO_TODOS_PRAZOS, "Atrasadas", "Vencem hoje", "Próximos 7 dias"
        });
        filtroPrazo.setPreferredSize(new Dimension(160, 30));
        filtroPrazo.addActionListener(e -> aplicarFiltro());

        JButton btnLimpar = buildSmallButton("Limpar filtros");
        btnLimpar.addActionListener(e -> limparFiltros());

        lblContador = new JLabel("0 tarefas");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.PLAIN, 12f));
        lblContador.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha1.setOpaque(false);
        linha1.add(campoPesquisa);
        linha1.add(filtroObra);
        linha1.add(filtroFuncionario);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha2.setOpaque(false);
        linha2.add(filtroEstado);
        linha2.add(filtroPrazo);
        linha2.add(btnLimpar);
        linha2.add(lblContador);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(linha1);
        left.add(Box.createVerticalStrut(8));
        left.add(linha2);

        JLabel dica = new JLabel("Dica: seleccione uma obra para rever rapidamente quem está responsável por cada tarefa.");
        dica.setForeground(UIManager.getColor("Label.disabledForeground"));
        dica.setFont(dica.getFont().deriveFont(11f));

        barra.add(left, BorderLayout.WEST);
        barra.add(dica, BorderLayout.SOUTH);
        return barra;
    }

    private JScrollPane buildTableArea() {
        modelo = new DefaultTableModel(
                new String[]{"ID", "Obra", "Equipa", "Funcionário", "Descrição", "Data limite", "Estado", "Prazo"},
                0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(UIConstants.TABLE_ROW_HEIGHT + 2);
        tabela.setShowVerticalLines(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setFillsViewportHeight(true);

        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);

        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(110);

        tabela.getColumnModel().getColumn(6).setCellRenderer(new EstadoRenderer());
        tabela.getColumnModel().getColumn(7).setCellRenderer(new PrazoRenderer());

        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0) {
                    abrirDetalhe();
                }
            }
        });

        return buildTablePane(tabela);
    }

    private JPanel buildActionsBar() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);

        JButton btnDetalhe = buildSmallButton("🔍 Ver detalhe");
        JButton btnEditar = buildSmallButton("✏ Editar");
        JButton btnEstado = buildSmallButton("⟳ Alterar estado");
        JButton btnReatribuir = buildSmallButton("👤 Reatribuir");
        JButton btnEliminar = buildSmallButton("🗑 Eliminar");

        btnEliminar.setForeground(UIConstants.COLOR_DANGER);

        btnDetalhe.addActionListener(e -> abrirDetalhe());
        btnEditar.addActionListener(e -> abrirDialogoEditar());
        btnEstado.addActionListener(e -> abrirDialogoEstado());
        btnReatribuir.addActionListener(e -> abrirDialogoReatribuir());
        btnEliminar.addActionListener(e -> eliminarSelecionada());

        barra.add(btnDetalhe);
        barra.add(btnEditar);
        barra.add(btnEstado);
        barra.add(btnReatribuir);
        barra.add(Box.createHorizontalStrut(12));
        barra.add(btnEliminar);
        return barra;
    }

    private void carregar() {
        obras = obraService.listarTodos().stream()
                .sorted(Comparator.comparing(this::obraLabel, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        funcionarios = funcionarioService.listarTodos().stream()
                .sorted(Comparator.comparing(Funcionario::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        estados = estadotarefaService.listarTodos().stream()
                .sorted(Comparator.comparing(Estadotarefa::getNomeEstado, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        equipas = equipaService.listarTodos().stream()
                .sorted(Comparator.comparing(this::equipaLabel, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        tarefasCarregadas = tarefaService.listarTodos().stream()
                .sorted(Comparator
                        .comparing(Tarefa::getDataLimite, Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparing(t -> safeLower(descricaoLabel(t))))
                .collect(Collectors.toList());

        repopularCombos();
        atualizarResumo();
        aplicarFiltro();
    }

    private void repopularCombos() {
        Object obraSelecionada = filtroObra != null ? filtroObra.getSelectedItem() : null;
        Object funcionarioSelecionado = filtroFuncionario != null ? filtroFuncionario.getSelectedItem() : null;
        Object estadoSelecionado = filtroEstado != null ? filtroEstado.getSelectedItem() : null;

        if (filtroObra != null) {
            filtroObra.removeAllItems();
            filtroObra.addItem(new ItemObra(null, FILTRO_TODAS_OBRAS));
            for (Obra obra : obras) filtroObra.addItem(new ItemObra(obra, obraLabel(obra)));
            restaurarSelecaoObra(obraSelecionada);
        }

        if (filtroFuncionario != null) {
            filtroFuncionario.removeAllItems();
            filtroFuncionario.addItem(new ItemFuncionario(null, FILTRO_TODOS_FUNCIONARIOS));
            for (Funcionario funcionario : funcionarios)
                filtroFuncionario.addItem(new ItemFuncionario(funcionario, funcionarioLabel(funcionario)));
            restaurarSelecaoFuncionario(funcionarioSelecionado);
        }

        if (filtroEstado != null) {
            filtroEstado.removeAllItems();
            filtroEstado.addItem(new ItemEstado(null, FILTRO_TODOS_ESTADOS));
            for (Estadotarefa estado : estados)
                filtroEstado.addItem(new ItemEstado(estado, estado.getNomeEstado()));
            restaurarSelecaoEstado(estadoSelecionado);
        }
    }

    private void restaurarSelecaoObra(Object anterior) {
        Integer id = anterior instanceof ItemObra item && item.obra != null ? item.obra.getId() : null;
        selecionarComboPorId(filtroObra, id);
    }

    private void restaurarSelecaoFuncionario(Object anterior) {
        Integer id = anterior instanceof ItemFuncionario item && item.funcionario != null ? item.funcionario.getId() : null;
        selecionarComboPorId(filtroFuncionario, id);
    }

    private void restaurarSelecaoEstado(Object anterior) {
        Integer id = anterior instanceof ItemEstado item && item.estado != null ? item.estado.getId() : null;
        selecionarComboPorId(filtroEstado, id);
    }

    private <T> void selecionarComboPorId(JComboBox<T> combo, Integer id) {
        if (combo == null || combo.getItemCount() == 0) return;
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof ItemObra io && io.obra != null && Objects.equals(io.obra.getId(), id)) {
                combo.setSelectedIndex(i);
                return;
            }
            if (item instanceof ItemFuncionario ifn && ifn.funcionario != null && Objects.equals(ifn.funcionario.getId(), id)) {
                combo.setSelectedIndex(i);
                return;
            }
            if (item instanceof ItemEstado ie && ie.estado != null && Objects.equals(ie.estado.getId(), id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private void atualizarResumo() {
        LocalDate hoje = LocalDate.now();
        long total = tarefasCarregadas.size();
        long atrasadas = tarefasCarregadas.stream().filter(this::isAtrasada).count();
        long hojeCount = tarefasCarregadas.stream()
                .filter(t -> hoje.equals(t.getDataLimite()))
                .count();
        long semana = tarefasCarregadas.stream()
                .filter(t -> t.getDataLimite() != null)
                .filter(t -> {
                    long dias = ChronoUnit.DAYS.between(hoje, t.getDataLimite());
                    return dias >= 0 && dias <= 7;
                })
                .count();

        lblTotalValor.setText(String.valueOf(total));
        lblAtrasadasValor.setText(String.valueOf(atrasadas));
        lblHojeValor.setText(String.valueOf(hojeCount));
        lblSemanaValor.setText(String.valueOf(semana));
    }

    private void aplicarFiltro() {
        String pesquisa = campoPesquisa != null ? safeLower(campoPesquisa.getText().trim()) : "";
        ItemObra obraSel = filtroObra != null ? (ItemObra) filtroObra.getSelectedItem() : null;
        ItemFuncionario funcSel = filtroFuncionario != null ? (ItemFuncionario) filtroFuncionario.getSelectedItem() : null;
        ItemEstado estadoSel = filtroEstado != null ? (ItemEstado) filtroEstado.getSelectedItem() : null;
        String prazoSel = filtroPrazo != null ? (String) filtroPrazo.getSelectedItem() : FILTRO_TODOS_PRAZOS;

        modelo.setRowCount(0);
        List<Tarefa> filtradas = new ArrayList<>();

        for (Tarefa tarefa : tarefasCarregadas) {
            if (obraSel != null && obraSel.obra != null) {
                if (tarefa.getIdObra() == null || !Objects.equals(tarefa.getIdObra().getId(), obraSel.obra.getId())) continue;
            }
            if (funcSel != null && funcSel.funcionario != null) {
                if (tarefa.getIdFuncionario() == null || !Objects.equals(tarefa.getIdFuncionario().getId(), funcSel.funcionario.getId())) continue;
            }
            if (estadoSel != null && estadoSel.estado != null) {
                if (tarefa.getIdEstadoTarefa() == null || !Objects.equals(tarefa.getIdEstadoTarefa().getId(), estadoSel.estado.getId())) continue;
            }
            if (!cumpreFiltroPrazo(tarefa, prazoSel)) continue;

            if (!pesquisa.isBlank()) {
                String descricao = safeLower(descricaoLabel(tarefa));
                String obra = safeLower(obraLabel(tarefa.getIdObra()));
                String equipa = safeLower(equipaLabel(tarefa.getIdEquipa()));
                String funcionario = safeLower(funcionarioLabel(tarefa.getIdFuncionario()));
                String estado = safeLower(estadoLabel(tarefa));
                if (!descricao.contains(pesquisa) && !obra.contains(pesquisa)
                        && !equipa.contains(pesquisa) && !funcionario.contains(pesquisa) && !estado.contains(pesquisa)) {
                    continue;
                }
            }

            filtradas.add(tarefa);
        }

        for (Tarefa tarefa : filtradas) {
            modelo.addRow(new Object[]{
                    tarefa.getId(),
                    obraLabel(tarefa.getIdObra()),
                    equipaLabel(tarefa.getIdEquipa()),
                    funcionarioLabel(tarefa.getIdFuncionario()),
                    truncar(descricaoLabel(tarefa), 72),
                    formatDate(tarefa.getDataLimite()),
                    estadoLabel(tarefa),
                    prazoLabel(tarefa)
            });
        }

        lblContador.setText(filtradas.size() + (filtradas.size() == 1 ? " tarefa" : " tarefas"));
    }

    private boolean cumpreFiltroPrazo(Tarefa tarefa, String prazoSel) {
        if (prazoSel == null || FILTRO_TODOS_PRAZOS.equals(prazoSel)) return true;
        return switch (prazoSel) {
            case "Atrasadas" -> isAtrasada(tarefa);
            case "Vencem hoje" -> tarefa.getDataLimite() != null && LocalDate.now().equals(tarefa.getDataLimite());
            case "Próximos 7 dias" -> {
                if (tarefa.getDataLimite() == null) yield false;
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), tarefa.getDataLimite());
                yield dias >= 0 && dias <= 7;
            }
            default -> true;
        };
    }

    private void limparFiltros() {
        campoPesquisa.setText("");
        filtroObra.setSelectedIndex(0);
        filtroFuncionario.setSelectedIndex(0);
        filtroEstado.setSelectedIndex(0);
        filtroPrazo.setSelectedIndex(0);
        aplicarFiltro();
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    private Tarefa tarefaSelecionada() {
        Integer id = idSelecionado();
        if (id == null) {
            mostrarErro(null, "Seleccione uma tarefa.");
            return null;
        }
        try {
            return tarefaService.buscarPorId(id);
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
            return null;
        }
    }

    private void abrirDialogoNova() {
        JDialog dlg = criarDialogo("Nova Tarefa");
        FormTarefa form = new FormTarefa(dlg, null);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!form.confirmado()) return;

        try {
            tarefaService.guardar(form.construir());
            carregar();
            mostrarSucesso(null, "Tarefa criada com sucesso.");
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
        }
    }

    private void abrirDialogoEditar() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) return;

        JDialog dlg = criarDialogo("Editar Tarefa");
        FormTarefa form = new FormTarefa(dlg, tarefa);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!form.confirmado()) return;

        try {
            Tarefa atualizada = form.construir();
            atualizada.setId(tarefa.getId());
            tarefaService.guardar(atualizada);
            carregar();
            mostrarSucesso(null, "Tarefa actualizada.");
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
        }
    }

    private void abrirDialogoEstado() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) return;

        JComboBox<ItemEstado> combo = new JComboBox<>();
        for (Estadotarefa estado : estados) combo.addItem(new ItemEstado(estado, estado.getNomeEstado()));
        if (combo.getItemCount() == 0) {
            mostrarErro(null, "Não existem estados de tarefa configurados.");
            return;
        }
        selecionarComboPorId(combo, tarefa.getIdEstadoTarefa() != null ? tarefa.getIdEstadoTarefa().getId() : null);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Novo estado para a tarefa:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        int r = JOptionPane.showConfirmDialog(this, panel, "Alterar Estado",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        ItemEstado selecionado = (ItemEstado) combo.getSelectedItem();
        if (selecionado == null || selecionado.estado == null) {
            mostrarErro(null, "Seleccione um estado válido.");
            return;
        }

        try {
            tarefa.setIdEstadoTarefa(selecionado.estado);
            tarefaService.guardar(tarefa);
            carregar();
            mostrarSucesso(null, "Estado actualizado.");
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
        }
    }

    private void abrirDialogoReatribuir() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) return;

        JComboBox<ItemFuncionario> combo = new JComboBox<>();
        if (tarefa.getIdEquipa() != null) {
            for (Equipafuncionario membro : equipafuncionarioService.buscarPorEquipa(tarefa.getIdEquipa())) {
                if (membro.getIdFuncionario() != null) {
                    combo.addItem(new ItemFuncionario(membro.getIdFuncionario(), funcionarioLabel(membro.getIdFuncionario())));
                }
            }
        }
        if (combo.getItemCount() == 0) {
            mostrarErro(null, "Não existem funcionários disponíveis na equipa desta tarefa.");
            return;
        }
        selecionarComboPorId(combo, tarefa.getIdFuncionario() != null ? tarefa.getIdFuncionario().getId() : null);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Novo responsável pela tarefa:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        int r = JOptionPane.showConfirmDialog(this, panel, "Reatribuir Tarefa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        ItemFuncionario selecionado = (ItemFuncionario) combo.getSelectedItem();
        if (selecionado == null || selecionado.funcionario == null) {
            mostrarErro(null, "Seleccione um funcionário válido.");
            return;
        }

        try {
            tarefa.setIdFuncionario(selecionado.funcionario);
            tarefaService.guardar(tarefa);
            carregar();
            mostrarSucesso(null, "Tarefa reatribuída.");
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
        }
    }

    private void abrirDetalhe() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) return;

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel info = new JPanel(new GridLayout(0, 2, 12, 8));
        info.add(buildInfoLabel("Obra", obraLabel(tarefa.getIdObra())));
        info.add(buildInfoLabel("Equipa", equipaLabel(tarefa.getIdEquipa())));
        info.add(buildInfoLabel("Funcionário", funcionarioLabel(tarefa.getIdFuncionario())));
        info.add(buildInfoLabel("Estado", estadoLabel(tarefa)));
        info.add(buildInfoLabel("Prazo", formatDate(tarefa.getDataLimite()) + "  ·  " + prazoLabel(tarefa)));

        JTextArea descricao = new JTextArea(descricaoLabel(tarefa));
        descricao.setWrapStyleWord(true);
        descricao.setLineWrap(true);
        descricao.setEditable(false);
        descricao.setOpaque(false);
        descricao.setBorder(BorderFactory.createTitledBorder("Descrição"));
        descricao.setFont(descricao.getFont().deriveFont(13f));

        root.add(info, BorderLayout.NORTH);
        root.add(descricao, BorderLayout.CENTER);

        JDialog dlg = criarDialogo("Detalhe da Tarefa #" + tarefa.getId());
        dlg.add(root);
        dlg.setSize(560, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildInfoLabel(String titulo, String valor) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 11f));
        lblTitulo.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(lblValor.getFont().deriveFont(13f));

        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblValor);
        return panel;
    }

    private void eliminarSelecionada() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) return;

        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar a tarefa \"" + truncar(descricaoLabel(tarefa), 60) + "\"?",
                "Eliminar tarefa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;

        try {
            tarefaService.eliminar(tarefa.getId());
            carregar();
        } catch (Exception ex) {
            mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
        }
    }

    private JDialog criarDialogo(String titulo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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

    private String obraLabel(Obra obra) {
        if (obra == null) return "—";
        String desc = obra.getDescricao() != null && !obra.getDescricao().isBlank()
                ? obra.getDescricao().trim() : "Obra #" + obra.getId();
        String cliente = obra.getIdCliente() != null ? obra.getIdCliente().getNome() : "Sem cliente";
        return truncar(desc, 44) + " · " + cliente;
    }

    private String funcionarioLabel(Funcionario funcionario) {
        return funcionario != null ? funcionario.getNome() : "—";
    }

    private String equipaLabel(Equipa equipa) {
        return equipa != null ? equipa.getNomeEquipa() : "—";
    }

    private String descricaoLabel(Tarefa tarefa) {
        if (tarefa == null || tarefa.getDescricao() == null || tarefa.getDescricao().isBlank()) {
            return "Sem descrição";
        }
        return tarefa.getDescricao().trim();
    }

    private String estadoLabel(Tarefa tarefa) {
        return tarefa != null && tarefa.getIdEstadoTarefa() != null
                ? tarefa.getIdEstadoTarefa().getNomeEstado()
                : "—";
    }

    private String formatDate(LocalDate data) {
        return data != null ? data.format(FMT) : "—";
    }

    private boolean isAtrasada(Tarefa tarefa) {
        if (tarefa == null || tarefa.getDataLimite() == null) return false;
        return tarefa.getDataLimite().isBefore(LocalDate.now()) && !estadoConcluido(tarefa);
    }

    private boolean estadoConcluido(Tarefa tarefa) {
        String estado = safeLower(estadoLabel(tarefa));
        return estado.contains("concl");
    }

    private String prazoLabel(Tarefa tarefa) {
        if (tarefa == null || tarefa.getDataLimite() == null) return "Sem prazo";
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), tarefa.getDataLimite());
        if (dias < 0) return "Atrasada";
        if (dias == 0) return "Hoje";
        if (dias == 1) return "Amanhã";
        return "Faltam " + dias + " dias";
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "—";
        if (texto.length() <= max) return texto;
        return texto.substring(0, Math.max(0, max - 1)).trim() + "…";
    }

    private String safeLower(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
    }

    private static class ItemObra {
        private final Obra obra;
        private final String label;

        private ItemObra(Obra obra, String label) {
            this.obra = obra;
            this.label = label;
        }

        @Override public String toString() { return label; }
    }

    private static class ItemFuncionario {
        private final Funcionario funcionario;
        private final String label;

        private ItemFuncionario(Funcionario funcionario, String label) {
            this.funcionario = funcionario;
            this.label = label;
        }

        @Override public String toString() { return label; }
    }

    private static class ItemEquipa {
        private final Equipa equipa;
        private final String label;

        private ItemEquipa(Equipa equipa, String label) {
            this.equipa = equipa;
            this.label = label;
        }

        @Override public String toString() { return label; }
    }

    private static class ItemEstado {
        private final Estadotarefa estado;
        private final String label;

        private ItemEstado(Estadotarefa estado, String label) {
            this.estado = estado;
            this.label = label;
        }

        @Override public String toString() { return label; }
    }

    private class FormTarefa extends JPanel {
        private final JDialog dialog;
        private boolean confirmado;

        private final JComboBox<ItemObra> cmbObra = new JComboBox<>();
        private final JComboBox<ItemEquipa> cmbEquipa = new JComboBox<>();
        private final JComboBox<ItemFuncionario> cmbFuncionario = new JComboBox<>();
        private final JComboBox<ItemEstado> cmbEstado = new JComboBox<>();
        private final JTextArea txtDescricao = new JTextArea(5, 34);
        private final JTextField txtDataLimite = new JTextField();

        private FormTarefa(JDialog dialog, Tarefa existente) {
            this.dialog = dialog;
            setLayout(new BorderLayout(0, 12));
            setBorder(new EmptyBorder(18, 18, 18, 18));

            preencherCombos();

            txtDescricao.setLineWrap(true);
            txtDescricao.setWrapStyleWord(true);
            txtDescricao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225)),
                    new EmptyBorder(8, 8, 8, 8)));
            txtDataLimite.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
            cmbObra.addActionListener(e -> atualizarEquipasDaObra());
            cmbEquipa.addActionListener(e -> atualizarFuncionariosDaEquipa());

            if (existente != null) {
                selecionarComboPorId(cmbObra, existente.getIdObra() != null ? existente.getIdObra().getId() : null);
                atualizarEquipasDaObra();
                selecionarComboEquipa(existente.getIdEquipa() != null ? existente.getIdEquipa().getId() : null);
                atualizarFuncionariosDaEquipa();
                selecionarComboPorId(cmbFuncionario, existente.getIdFuncionario() != null ? existente.getIdFuncionario().getId() : null);
                selecionarComboPorId(cmbEstado, existente.getIdEstadoTarefa() != null ? existente.getIdEstadoTarefa().getId() : null);
                txtDescricao.setText(existente.getDescricao());
                txtDataLimite.setText(formatDate(existente.getDataLimite()));
            } else if (cmbEstado.getItemCount() > 0) {
                atualizarEquipasDaObra();
                atualizarFuncionariosDaEquipa();
                ItemEstado sugerido = procurarEstadoPorNome("Pendente");
                cmbEstado.setSelectedItem(sugerido != null ? sugerido : cmbEstado.getItemAt(0));
            }

            add(buildForm(), BorderLayout.CENTER);
            add(buildButtons(), BorderLayout.SOUTH);
        }

        private void preencherCombos() {
            for (Obra obra : obras) cmbObra.addItem(new ItemObra(obra, obraLabel(obra)));
            for (Estadotarefa estado : estados)
                cmbEstado.addItem(new ItemEstado(estado, estado.getNomeEstado()));
        }

        private void atualizarEquipasDaObra() {
            cmbEquipa.removeAllItems();
            ItemObra itemObra = (ItemObra) cmbObra.getSelectedItem();
            if (itemObra == null || itemObra.obra == null) return;
            for (Equipa equipa : equipas) {
                if (equipa.getIdObra() != null && Objects.equals(equipa.getIdObra().getId(), itemObra.obra.getId())) {
                    cmbEquipa.addItem(new ItemEquipa(equipa, equipaLabel(equipa)));
                }
            }
        }

        private void atualizarFuncionariosDaEquipa() {
            cmbFuncionario.removeAllItems();
            ItemEquipa itemEquipa = (ItemEquipa) cmbEquipa.getSelectedItem();
            if (itemEquipa == null || itemEquipa.equipa == null) return;
            List<Equipafuncionario> membros = equipafuncionarioService.buscarPorEquipa(itemEquipa.equipa);
            for (Equipafuncionario membro : membros) {
                if (membro.getIdFuncionario() != null) {
                    cmbFuncionario.addItem(new ItemFuncionario(membro.getIdFuncionario(), funcionarioLabel(membro.getIdFuncionario())));
                }
            }
        }

        private void selecionarComboEquipa(Integer idEquipa) {
            if (idEquipa == null) return;
            for (int i = 0; i < cmbEquipa.getItemCount(); i++) {
                ItemEquipa item = cmbEquipa.getItemAt(i);
                if (item.equipa != null && Objects.equals(item.equipa.getId(), idEquipa)) {
                    cmbEquipa.setSelectedIndex(i);
                    return;
                }
            }
        }

        private ItemEstado procurarEstadoPorNome(String nome) {
            for (int i = 0; i < cmbEstado.getItemCount(); i++) {
                ItemEstado item = cmbEstado.getItemAt(i);
                if (item.estado != null && item.estado.getNomeEstado() != null
                        && item.estado.getNomeEstado().equalsIgnoreCase(nome)) {
                    return item;
                }
            }
            return null;
        }

        private JPanel buildForm() {
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;

            int y = 0;
            addField(form, gbc, y++, "Obra", cmbObra);
            addField(form, gbc, y++, "Equipa", cmbEquipa);
            addField(form, gbc, y++, "Funcionário", cmbFuncionario);
            addField(form, gbc, y++, "Estado", cmbEstado);
            addField(form, gbc, y++, "Data limite", txtDataLimite);

            gbc.gridx = 0;
            gbc.gridy = y;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblDesc = new JLabel("Descrição");
            lblDesc.setFont(lblDesc.getFont().deriveFont(Font.BOLD, 12f));
            form.add(lblDesc, gbc);

            gbc.gridy = y + 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane scroll = new JScrollPane(txtDescricao);
            Border border = BorderFactory.createLineBorder(new Color(203, 213, 225));
            scroll.setBorder(border);
            form.add(scroll, gbc);
            return form;
        }

        private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 10, 10);

            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            form.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            field.setPreferredSize(new Dimension(320, 32));
            form.add(field, gbc);
        }

        private JPanel buildButtons() {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);

            JButton btnCancelar = buildButton("Cancelar");
            btnCancelar.addActionListener(e -> dialog.dispose());

            JButton btnGuardar = buildButton("Guardar");
            btnGuardar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnGuardar.setForeground(Color.WHITE);
            btnGuardar.setOpaque(true);
            btnGuardar.setBorderPainted(false);
            btnGuardar.addActionListener(e -> {
                if (!validar()) return;
                confirmado = true;
                dialog.dispose();
            });

            buttons.add(btnCancelar);
            buttons.add(btnGuardar);
            return buttons;
        }

        private boolean validar() {
            if (cmbObra.getSelectedItem() == null) {
                mostrarErro(dialog, "Seleccione uma obra.");
                return false;
            }
            if (cmbFuncionario.getSelectedItem() == null) {
                mostrarErro(dialog, "Seleccione um funcionário.");
                return false;
            }
            if (cmbEquipa.getSelectedItem() == null) {
                mostrarErro(dialog, "Seleccione uma equipa.");
                return false;
            }
            if (cmbEstado.getSelectedItem() == null) {
                mostrarErro(dialog, "Seleccione um estado.");
                return false;
            }
            if (txtDescricao.getText().trim().isBlank()) {
                mostrarErro(dialog, "A descrição da tarefa é obrigatória.");
                return false;
            }
            try {
                LocalDate.parse(txtDataLimite.getText().trim(), FMT);
            } catch (DateTimeParseException ex) {
                mostrarErro(dialog, "Data limite inválida. Use o formato dd/MM/yyyy.");
                return false;
            }
            return true;
        }

        private Tarefa construir() {
            Tarefa tarefa = new Tarefa();
            tarefa.setIdObra(((ItemObra) cmbObra.getSelectedItem()).obra);
            tarefa.setIdEquipa(((ItemEquipa) cmbEquipa.getSelectedItem()).equipa);
            tarefa.setIdFuncionario(((ItemFuncionario) cmbFuncionario.getSelectedItem()).funcionario);
            tarefa.setIdEstadoTarefa(((ItemEstado) cmbEstado.getSelectedItem()).estado);
            tarefa.setDescricao(txtDescricao.getText().trim());
            tarefa.setDataLimite(LocalDate.parse(txtDataLimite.getText().trim(), FMT));
            return tarefa;
        }

        private boolean confirmado() {
            return confirmado;
        }
    }

    private class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = value != null ? value.toString() : "";
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                label.setForeground(colorEstadoTexto(estado));
                label.setBackground(colorEstadoFundo(estado));
            }
            return label;
        }

        private Color colorEstadoTexto(String estado) {
            String e = safeLower(estado);
            if (e.contains("concl")) return new Color(21, 128, 61);
            if (e.contains("exec")) return new Color(29, 78, 216);
            if (e.contains("atras")) return new Color(185, 28, 28);
            if (e.contains("pend")) return new Color(146, 64, 14);
            return UIManager.getColor("Label.foreground");
        }

        private Color colorEstadoFundo(String estado) {
            String e = safeLower(estado);
            if (e.contains("concl")) return new Color(220, 252, 231);
            if (e.contains("exec")) return new Color(219, 234, 254);
            if (e.contains("atras")) return new Color(254, 226, 226);
            if (e.contains("pend")) return new Color(255, 237, 213);
            return Color.WHITE;
        }
    }

    private class PrazoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String prazo = value != null ? value.toString() : "";
            if (!isSelected) {
                if ("Atrasada".equalsIgnoreCase(prazo)) label.setForeground(UIConstants.COLOR_DANGER);
                else if ("Hoje".equalsIgnoreCase(prazo) || "Amanhã".equalsIgnoreCase(prazo)) label.setForeground(UIConstants.COLOR_WARNING);
                else label.setForeground(UIManager.getColor("Label.foreground"));
            }
            return label;
        }
    }
}
