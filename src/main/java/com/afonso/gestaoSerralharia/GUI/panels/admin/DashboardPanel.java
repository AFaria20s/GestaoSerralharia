package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends BasePanel {

    private final ObraService        obraService;
    private final TarefaService      tarefaService;
    private final FaturaService      faturaService;
    private final ProblemaService    problemaService;
    private final ClienteService     clienteService;
    private final FuncionarioService funcionarioService;
    private final OrcamentoService   orcamentoService;
    private final VisitaService      visitaService;

    // KPI labels
    private JLabel lblObrasValor;
    private JLabel lblObrasSubtitulo;
    private JLabel lblTarefasValor;
    private JLabel lblTarefasSubtitulo;
    private JLabel lblFaturasValor;
    private JLabel lblFaturasSubtitulo;
    private JLabel lblOrcamentosValor;
    private JLabel lblOrcamentosSubtitulo;
    private JLabel lblVisitasValor;
    private JLabel lblVisitasSubtitulo;
    private JLabel lblProblemasValor;
    private JLabel lblProblemasSubtitulo;

    // Table badges
    private JLabel lblBadgeObras;
    private JLabel lblBadgeTarefas;

    // Table models
    private DefaultTableModel modeloObras;
    private DefaultTableModel modeloTarefas;

    // Bottom section panels
    private JPanel painelOrcamentos;
    private JPanel painelVisitas;
    private JPanel painelProblemas;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public DashboardPanel(ObraService obraService, TarefaService tarefaService,
                          FaturaService faturaService, ProblemaService problemaService,
                          ClienteService clienteService, FuncionarioService funcionarioService,
                          OrcamentoService orcamentoService, VisitaService visitaService) {
        this.obraService        = obraService;
        this.tarefaService      = tarefaService;
        this.faturaService      = faturaService;
        this.problemaService    = problemaService;
        this.clienteService     = clienteService;
        this.funcionarioService = funcionarioService;
        this.orcamentoService   = orcamentoService;
        this.visitaService      = visitaService;

        String nome     = SessionManager.getInstance().getNome();
        String saudacao = saudacao() + (nome != null && !nome.isBlank() ? ", " + nome.split(" ")[0] : "");

        JButton btnRefresh = buildButton("Actualizar");
        btnRefresh.addActionListener(e -> carregar());

        add(buildHeader("Dashboard", saudacao, btnRefresh), BorderLayout.NORTH);
        add(buildScroll(), BorderLayout.CENTER);
        carregar();
    }

    // ─────────────────────────────────────────────────────────────
    //  Layout
    // ─────────────────────────────────────────────────────────────

    private JScrollPane buildScroll() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setOpaque(false);

        corpo.add(buildSecaoKpi());
        corpo.add(Box.createVerticalStrut(16));
        corpo.add(buildFaixaOperacional());
        corpo.add(Box.createVerticalStrut(24));
        corpo.add(buildSecaoTabelas());
        corpo.add(Box.createVerticalStrut(24));
        corpo.add(buildSecaoInferior());
        corpo.add(Box.createVerticalStrut(16));

        JScrollPane scroll = new JScrollPane(corpo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────
    //  KPI cards (topo)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildSecaoKpi() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        grid.setAlignmentX(LEFT_ALIGNMENT);

        lblObrasValor          = new JLabel("—");
        lblObrasSubtitulo      = new JLabel(" ");
        lblTarefasValor        = new JLabel("—");
        lblTarefasSubtitulo    = new JLabel(" ");
        lblFaturasValor        = new JLabel("—");
        lblFaturasSubtitulo    = new JLabel(" ");
        lblOrcamentosValor     = new JLabel("—");
        lblOrcamentosSubtitulo = new JLabel(" ");
        lblVisitasValor        = new JLabel("—");
        lblVisitasSubtitulo    = new JLabel(" ");
        lblProblemasValor      = new JLabel("—");
        lblProblemasSubtitulo  = new JLabel(" ");

        grid.add(buildKpiCard("Obras em execucao",     lblObrasValor,      lblObrasSubtitulo,      UIConstants.COLOR_INFO,    new Color(59, 130, 246)));
        grid.add(buildKpiCard("Tarefas atrasadas",     lblTarefasValor,    lblTarefasSubtitulo,    UIConstants.COLOR_DANGER,  new Color(220, 38, 38)));
        grid.add(buildKpiCard("Valor por receber",     lblFaturasValor,    lblFaturasSubtitulo,    UIConstants.COLOR_WARNING, new Color(217, 119, 6)));
        grid.add(buildKpiCard("Orcamentos p/ aprovar", lblOrcamentosValor, lblOrcamentosSubtitulo, UIConstants.COLOR_SUCCESS, new Color(22, 163, 74)));

        return grid;
    }

    private JPanel buildFaixaOperacional() {
        JPanel faixa = new JPanel(new GridLayout(1, 2, 16, 0));
        faixa.setOpaque(false);
        faixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        faixa.setAlignmentX(LEFT_ALIGNMENT);

        faixa.add(buildResumoCard("Agenda imediata", lblVisitasValor, lblVisitasSubtitulo, UIConstants.COLOR_INFO));
        faixa.add(buildResumoCard("Alertas operacionais", lblProblemasValor, lblProblemasSubtitulo, UIConstants.COLOR_DANGER));
        return faixa;
    }

    private JPanel buildResumoCard(String titulo, JLabel valor, JLabel sub, Color cor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 13f));

        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 24f));
        valor.setForeground(cor);
        sub.setFont(sub.getFont().deriveFont(11f));
        sub.setForeground(UIManager.getColor("Label.disabledForeground"));

        card.add(lblTit, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        card.add(sub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildKpiCard(String titulo, JLabel lblValor, JLabel lblSub, Color corValor, Color corDot) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel dot = new JLabel("●");
        dot.setForeground(corDot);
        dot.setFont(dot.getFont().deriveFont(9f));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.PLAIN, 12f));
        lblTit.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topo.setOpaque(false);
        topo.add(dot);
        topo.add(lblTit);

        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 34f));
        lblValor.setForeground(corValor);

        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 11f));
        lblSub.setForeground(corValor);

        JPanel centro = new JPanel(new BorderLayout(0, 2));
        centro.setOpaque(false);
        centro.add(lblValor, BorderLayout.CENTER);
        centro.add(lblSub,   BorderLayout.SOUTH);

        card.add(topo,   BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────
    //  Tabelas (meio)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildSecaoTabelas() {
        JPanel linha = new JPanel(new GridLayout(1, 2, 16, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        lblBadgeObras   = buildBadge("—", UIConstants.COLOR_SUCCESS);
        lblBadgeTarefas = buildBadge("—", UIConstants.COLOR_DANGER);

        linha.add(buildTabelaObras());
        linha.add(buildTabelaTarefas());
        return linha;
    }

    private JPanel buildTabelaObras() {
        modeloObras = new DefaultTableModel(new String[]{"Obra", "Cliente", "Inicio"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return buildCartaoTabela("Obras em execucao", lblBadgeObras, miniTabela(modeloObras));
    }

    private JPanel buildTabelaTarefas() {
        modeloTarefas = new DefaultTableModel(new String[]{"Tarefa", "Funcionario", "Limite"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = miniTabela(modeloTarefas);
        tabela.getColumnModel().getColumn(2).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel l = new JLabel(v != null ? v.toString() : "");
            l.setFont(t.getFont().deriveFont(12f));
            l.setForeground(sel ? t.getSelectionForeground() : UIConstants.COLOR_DANGER);
            l.setBorder(new EmptyBorder(0, 8, 0, 8));
            l.setOpaque(true);
            l.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return l;
        });

        return buildCartaoTabela("Tarefas atrasadas", lblBadgeTarefas, tabela);
    }

    private JPanel buildCartaoTabela(String titulo, JLabel badge, JTable tabela) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 13f));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTit, BorderLayout.WEST);
        header.add(badge,  BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIManager.getColor("Panel.background"));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────
    //  Seccao inferior: Orcamentos | Visitas | Problemas
    // ─────────────────────────────────────────────────────────────

    private JPanel buildSecaoInferior() {
        JPanel linha = new JPanel(new GridLayout(1, 3, 16, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        painelOrcamentos = buildCardComBadge("Orcamentos p/ aprovar", "listaOrcamentos", "— pendentes", UIConstants.COLOR_SUCCESS);
        painelVisitas    = buildCardComBadge("Visitas hoje e amanha",  "listaVisitas",    "— hoje/amanha", UIConstants.COLOR_INFO);
        painelProblemas  = buildCardComBadge("Problemas reportados",   "listaProblemas",  "— abertos",    UIConstants.COLOR_DANGER);

        linha.add(painelOrcamentos);
        linha.add(painelVisitas);
        linha.add(painelProblemas);
        return linha;
    }

    /** Cria um card com badge actualizavel, guardando o badge como client property */
    private JPanel buildCardComBadge(String titulo, String listName, String badgeInicial, Color badgeCor) {
        JLabel badge = buildBadge(badgeInicial, badgeCor);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(cardTitulo(titulo), BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        JPanel lista = listaPanel(listName);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.add(header, BorderLayout.NORTH);
        card.add(lista,  BorderLayout.CENTER);
        // Guardar referencia ao badge para actualizacoes simples
        card.putClientProperty("badge", badge);
        return card;
    }

    // ─────────────────────────────────────────────────────────────
    //  Carregamento de dados
    // ─────────────────────────────────────────────────────────────

    private void carregar() {
        carregarKpis();
        carregarTabelaObras();
        carregarTabelaTarefas();
        carregarOrcamentos();
        carregarVisitas();
        carregarProblemas();
    }

    private void carregarKpis() {
        // Obras em execucao
        List<Obra> emExec = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .collect(Collectors.toList());
        lblObrasValor.setText(String.valueOf(emExec.size()));
        lblObrasSubtitulo.setText(emExec.isEmpty() ? " " : emExec.size() + " obras ativas");

        // Tarefas atrasadas
        List<Tarefa> atrasadas = tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null) return true;
                    String est = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !est.contains("conclu") && !est.contains("cancel");
                })
                .collect(Collectors.toList());
        lblTarefasValor.setText(String.valueOf(atrasadas.size()));
        lblTarefasSubtitulo.setText(atrasadas.isEmpty() ? " " : atrasadas.size() + " tarefa(s) em atraso");

        // Valor por receber (faturas)
        List<Fatura> faturasPendentes = faturaService.listarTodos().stream()
                .filter(f -> f.getValorTotalComIva() != null && f.getValorPago() != null
                        && f.getValorPago().compareTo(f.getValorTotalComIva()) < 0)
                .collect(Collectors.toList());
        BigDecimal totalPorReceber = faturasPendentes.stream()
                .map(f -> f.getValorTotalComIva().subtract(f.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long valorLong = totalPorReceber.longValue();
        String valorStr = valorLong >= 1000
                ? "€ " + String.format("%,d", valorLong).replace(",", ".")
                : "€ " + valorLong;
        lblFaturasValor.setText(valorStr);
        lblFaturasSubtitulo.setText(faturasPendentes.size() + " fatura(s) pendente(s)");

        // Orcamentos por aprovar (corrigido: usa OrcamentoService)
        List<Orcamento> orcPorAprovar = orcamentoService.listarTodos().stream()
                .filter(o -> o.getAprovado() != null && !o.getAprovado())
                .collect(Collectors.toList());
        int qtdOrc = orcPorAprovar.size();
        lblOrcamentosValor.setText(qtdOrc > 9 ? "9+" : String.valueOf(qtdOrc));
        lblOrcamentosSubtitulo.setText(qtdOrc == 0 ? "nenhum pendente" : qtdOrc + " aguardam aprovacao");

        LocalDate hoje = LocalDate.now();
        long visitasHojeAmanha = visitaService.listarTodos().stream()
                .filter(v -> v.getDataVisita() != null)
                .map(v -> v.getDataVisita().atZone(ZoneId.systemDefault()).toLocalDate())
                .filter(d -> !d.isBefore(hoje) && !d.isAfter(hoje.plusDays(1)))
                .count();
        lblVisitasValor.setText(String.valueOf(visitasHojeAmanha));
        lblVisitasSubtitulo.setText(visitasHojeAmanha == 0 ? "sem deslocações imediatas" : "visitas para hoje e amanhã");

        List<Problema> problemas = problemaService.listarTodos();
        long problemasAltos = problemas.stream()
                .filter(p -> p.getIdGravidade() != null)
                .filter(p -> {
                    String g = p.getIdGravidade().getNomeGravidade().toLowerCase();
                    return g.contains("alta") || g.contains("crit");
                })
                .count();
        lblProblemasValor.setText(String.valueOf(problemas.size()));
        lblProblemasSubtitulo.setText(problemasAltos > 0
                ? problemasAltos + " de gravidade alta"
                : "sem alertas graves");
    }

    private void carregarTabelaObras() {
        modeloObras.setRowCount(0);
        List<Obra> emExec = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .limit(6)
                .collect(Collectors.toList());

        lblBadgeObras.setText(emExec.size() + " ativas");

        for (Obra o : emExec) {
            String cliente = o.getIdCliente() != null ? o.getIdCliente().getNome() : "—";
            String desc    = o.getDescricao() != null && !o.getDescricao().isBlank()
                    ? truncar(o.getDescricao(), 36) : "#" + o.getId();
            String data    = o.getDataCriacao() != null ? o.getDataCriacao().format(FMT_DATA) : "—";
            modeloObras.addRow(new Object[]{desc, cliente, data});
        }

        if (modeloObras.getRowCount() == 0)
            modeloObras.addRow(new Object[]{"Nenhuma obra em execucao", "", ""});
    }

    private void carregarTabelaTarefas() {
        modeloTarefas.setRowCount(0);
        List<Tarefa> atrasadas = tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null) return true;
                    String est = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !est.contains("conclu") && !est.contains("cancel");
                })
                .limit(6)
                .collect(Collectors.toList());

        lblBadgeTarefas.setText(atrasadas.size() + " em atraso");

        for (Tarefa t : atrasadas) {
            String func = t.getIdFuncionario() != null ? t.getIdFuncionario().getNome() : "—";
            String desc = t.getDescricao() != null && !t.getDescricao().isBlank()
                    ? truncar(t.getDescricao(), 32) : "#" + t.getId();
            String data = t.getDataLimite() != null ? t.getDataLimite().format(FMT_DATA) : "—";
            if (t.getIdObra() != null && t.getIdObra().getDescricao() != null && !t.getIdObra().getDescricao().isBlank()) {
                desc = desc + " · " + truncar(t.getIdObra().getDescricao(), 16);
            }
            modeloTarefas.addRow(new Object[]{desc, func, data});
        }

        if (modeloTarefas.getRowCount() == 0)
            modeloTarefas.addRow(new Object[]{"Sem tarefas atrasadas", "", ""});
    }

    /** Orcamentos ainda nao aprovados — corrigido: usa OrcamentoService, nao FaturaService */
    private void carregarOrcamentos() {
        JPanel lista = findNamed(painelOrcamentos, "listaOrcamentos");
        if (lista == null) return;
        lista.removeAll();

        List<Orcamento> pendentes = orcamentoService.listarTodos().stream()
                .filter(o -> o.getAprovado() != null && !o.getAprovado())
                .sorted((a, b) -> {
                    LocalDate da = a.getDataEmissao() != null ? a.getDataEmissao() : LocalDate.MIN;
                    LocalDate db = b.getDataEmissao() != null ? b.getDataEmissao() : LocalDate.MIN;
                    return db.compareTo(da);
                })
                .limit(4)
                .collect(Collectors.toList());

        atualizarBadge(painelOrcamentos,
                pendentes.isEmpty() ? "0 pendentes" : pendentes.size() + " pendente(s)");

        if (pendentes.isEmpty()) {
            lista.add(labelVazio("Sem orcamentos por aprovar"));
        } else {
            for (Orcamento o : pendentes) {
                lista.add(buildLinhaOrcamento(o));
                lista.add(Box.createVerticalStrut(10));
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaOrcamento(Orcamento o) {
        String tituloObra = (o.getIdObra() != null && o.getIdObra().getDescricao() != null)
                ? truncar(o.getIdObra().getDescricao(), 28) : "Orcamento #" + o.getId();
        String sub = "V" + (o.getVersao() != null ? o.getVersao() : 1) + " · emitido em " +
                (o.getDataEmissao() != null ? o.getDataEmissao().format(FMT_FULL) : "—");

        JButton btnVer = buildSmallButton("Ver");
        btnVer.setPreferredSize(new Dimension(56, 36));

        JPanel linha = new JPanel(new BorderLayout(8, 2));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.add(buildTexto(tituloObra, sub), BorderLayout.CENTER);
        linha.add(btnVer,                      BorderLayout.EAST);
        return linha;
    }

    /** Visitas para hoje e amanha, usando VisitaService */
    private void carregarVisitas() {
        JPanel lista = findNamed(painelVisitas, "listaVisitas");
        if (lista == null) return;
        lista.removeAll();

        LocalDate hoje   = LocalDate.now();
        LocalDate amanha = hoje.plusDays(1);

        List<Visita> visitasProximas = visitaService.listarTodos().stream()
                .filter(v -> {
                    if (v.getDataVisita() == null) return false;
                    LocalDate dv = v.getDataVisita().atZone(ZoneId.systemDefault()).toLocalDate();
                    return dv.equals(hoje) || dv.equals(amanha);
                })
                .sorted((a, b) -> a.getDataVisita().compareTo(b.getDataVisita()))
                .limit(5)
                .collect(Collectors.toList());

        atualizarBadge(painelVisitas,
                visitasProximas.isEmpty() ? "0 hoje/amanha" : visitasProximas.size() + " agendada(s)");

        if (visitasProximas.isEmpty()) {
            lista.add(labelVazio("Sem visitas para hoje ou amanha"));
        } else {
            for (Visita v : visitasProximas) {
                lista.add(buildLinhaVisita(v, hoje));
                lista.add(separador());
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaVisita(Visita v, LocalDate hoje) {
        LocalDate dv     = v.getDataVisita().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean   isHoje = dv.equals(hoje);

        String hora     = FMT_HORA.format(v.getDataVisita().atZone(ZoneId.systemDefault()).toLocalTime());
        String diaLabel = isHoje ? "hoje" : "amanha";
        Color  corDot   = isHoje ? UIConstants.COLOR_INFO : new Color(150, 150, 150);

        String titulo  = (v.getIdObra() != null && v.getIdObra().getDescricao() != null)
                ? truncar(v.getIdObra().getDescricao(), 30) : "Visita #" + v.getId();
        String cliente = (v.getIdObra() != null && v.getIdObra().getIdCliente() != null)
                ? v.getIdObra().getIdCliente().getNome() : "—";

        JLabel dot = new JLabel("●");
        dot.setForeground(corDot);
        dot.setFont(dot.getFont().deriveFont(10f));
        dot.setVerticalAlignment(SwingConstants.TOP);

        JLabel lblHora = new JLabel(hora);
        lblHora.setFont(lblHora.getFont().deriveFont(Font.PLAIN, 12f));
        lblHora.setForeground(UIManager.getColor("Label.disabledForeground"));
        lblHora.setVerticalAlignment(SwingConstants.TOP);

        JPanel linha = new JPanel(new BorderLayout(8, 0));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.add(dot,                                          BorderLayout.WEST);
        linha.add(buildTexto(titulo, cliente + " · " + diaLabel), BorderLayout.CENTER);
        linha.add(lblHora,                                      BorderLayout.EAST);
        return linha;
    }

    private void carregarProblemas() {
        JPanel lista = findNamed(painelProblemas, "listaProblemas");
        if (lista == null) return;
        lista.removeAll();

        List<Problema> problemas = problemaService.listarTodos().stream()
                .limit(4)
                .collect(Collectors.toList());

        long criticos = problemas.stream()
                .filter(p -> p.getIdGravidade() != null &&
                        p.getIdGravidade().getNomeGravidade().toLowerCase().contains("crit"))
                .count();

        String badgeTexto = criticos > 0
                ? criticos + " critico" + (criticos > 1 ? "s" : "")
                : problemas.size() + " aberto" + (problemas.size() != 1 ? "s" : "");
        atualizarBadge(painelProblemas, badgeTexto);

        if (problemas.isEmpty()) {
            lista.add(labelVazio("Sem problemas reportados"));
        } else {
            for (Problema p : problemas) {
                lista.add(buildLinhaProblema(p));
                lista.add(separador());
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaProblema(Problema p) {
        String descricao = p.getDescricao() != null ? truncar(p.getDescricao(), 28) : "#" + p.getId();
        String obraDesc  = (p.getIdObra() != null && p.getIdObra().getDescricao() != null)
                ? truncar(p.getIdObra().getDescricao(), 28) : "—";

        String nomeGrav = p.getIdGravidade() != null ? p.getIdGravidade().getNomeGravidade() : "—";
        JLabel badge    = buildBadge(nomeGrav, gravColor(nomeGrav));

        JPanel linha = new JPanel(new BorderLayout(8, 2));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.add(buildTexto(descricao, obraDesc), BorderLayout.CENTER);
        linha.add(badge,                           BorderLayout.EAST);
        return linha;
    }

    // ─────────────────────────────────────────────────────────────
    //  Utilitarios de UI
    // ─────────────────────────────────────────────────────────────

    private JLabel cardTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        return lbl;
    }

    private JPanel listaPanel(String name) {
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setName(name);
        return lista;
    }

    private JPanel buildTexto(String linha1, String linha2) {
        JLabel lblTit = new JLabel(linha1);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel lblSub = new JLabel(linha2);
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 11f));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel texto = new JPanel(new GridLayout(2, 1, 0, 2));
        texto.setOpaque(false);
        texto.add(lblTit);
        texto.add(lblSub);
        return texto;
    }

    private JLabel labelVazio(String mensagem) {
        JLabel lbl = new JLabel(mensagem);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        return lbl;
    }

    private Color gravColor(String nome) {
        if (nome == null) return UIConstants.COLOR_INFO;
        String n = nome.toLowerCase();
        if (n.contains("crit")) return UIConstants.COLOR_DANGER;
        if (n.contains("alt"))  return UIConstants.COLOR_WARNING;
        return new Color(100, 116, 139);
    }

    private JLabel buildBadge(String texto, Color cor) {
        JLabel badge = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
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
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }

    /** Actualiza o texto do badge guardado como client property no card */
    private void atualizarBadge(JPanel card, String texto) {
        Object prop = card.getClientProperty("badge");
        if (prop instanceof JLabel) {
            ((JLabel) prop).setText(texto);
        }
    }

    private JPanel separador() {
        JPanel sep = new JPanel();
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(new MatteBorder(0, 0, 1, 0, borderColor()));
        return sep;
    }

    private JTable miniTabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setFont(t.getFont().deriveFont(12f));
        t.getTableHeader().setFont(t.getFont().deriveFont(Font.BOLD, 11f));
        return t;
    }

    private JPanel findNamed(JPanel root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel && name.equals(c.getName())) return (JPanel) c;
            if (c instanceof JPanel) {
                JPanel found = findNamed((JPanel) c, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Color borderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : new Color(226, 232, 240);
    }

    private static String saudacao() {
        int h = LocalTime.now().getHour();
        if (h < 12) return "Bom dia";
        if (h < 19) return "Boa tarde";
        return "Boa noite";
    }

    private static String truncar(String s, int max) {
        return s == null ? "—" : (s.length() > max ? s.substring(0, max - 1) + "…" : s);
    }
}
