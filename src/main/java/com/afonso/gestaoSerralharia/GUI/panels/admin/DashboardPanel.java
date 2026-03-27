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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard do administrador — resumo geral da empresa.
 *
 * Secções:
 *  • KPI cards  — Obras em execução, Tarefas atrasadas, Valor por receber, Orçamentos p/ aprovar
 *  • Obras em execução (tabela compacta)
 *  • Tarefas atrasadas (tabela compacta)
 *  • Orçamentos p/ aprovar
 *  • Visitas hoje e amanhã
 *  • Problemas reportados
 */
public class DashboardPanel extends BasePanel {

    // ── Serviços ─────────────────────────────────────────────────────────────
    private final ObraService        obraService;
    private final TarefaService      tarefaService;
    private final FaturaService      faturaService;
    private final ProblemaService    problemaService;
    private final ClienteService     clienteService;
    private final FuncionarioService funcionarioService;

    // ── KPI labels ────────────────────────────────────────────────────────────
    private JLabel lblObrasValor;
    private JLabel lblObrasSubtitulo;
    private JLabel lblTarefasValor;
    private JLabel lblTarefasSubtitulo;
    private JLabel lblFaturasValor;
    private JLabel lblFaturasSubtitulo;
    private JLabel lblOrcamentosValor;
    private JLabel lblOrcamentosSubtitulo;

    // ── Badges das tabelas ────────────────────────────────────────────────────
    private JLabel lblBadgeObras;
    private JLabel lblBadgeTarefas;

    // ── Tabelas ───────────────────────────────────────────────────────────────
    private DefaultTableModel modeloObras;
    private DefaultTableModel modeloTarefas;

    // ── Painéis inferiores ────────────────────────────────────────────────────
    private JPanel painelOrcamentos;
    private JPanel painelVisitas;
    private JPanel painelProblemas;

    private static final DateTimeFormatter FMT_DATA  = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_FULL  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────────

    public DashboardPanel(ObraService obraService, TarefaService tarefaService,
                          FaturaService faturaService, ProblemaService problemaService,
                          ClienteService clienteService, FuncionarioService funcionarioService) {
        this.obraService        = obraService;
        this.tarefaService      = tarefaService;
        this.faturaService      = faturaService;
        this.problemaService    = problemaService;
        this.clienteService     = clienteService;
        this.funcionarioService = funcionarioService;

        String nome     = SessionManager.getInstance().getNome();
        String saudacao = saudacao() + (nome != null && !nome.isBlank() ? ", " + nome.split(" ")[0] : "");

        JButton btnRefresh = buildButton("↻ Actualizar");
        btnRefresh.addActionListener(e -> carregar());

        add(buildHeader("Dashboard", saudacao, btnRefresh), BorderLayout.NORTH);
        add(buildScroll(), BorderLayout.CENTER);
        carregar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCROLL + CORPO
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildScroll() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setOpaque(false);

        corpo.add(buildSecaoKpi());
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

    // ─────────────────────────────────────────────────────────────────────────
    //  KPI CARDS
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildSecaoKpi() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        grid.setAlignmentX(LEFT_ALIGNMENT);

        lblObrasValor       = new JLabel("—");
        lblObrasSubtitulo   = new JLabel(" ");
        lblTarefasValor     = new JLabel("—");
        lblTarefasSubtitulo = new JLabel(" ");
        lblFaturasValor     = new JLabel("—");
        lblFaturasSubtitulo = new JLabel(" ");
        lblOrcamentosValor  = new JLabel("—");
        lblOrcamentosSubtitulo = new JLabel(" ");

        grid.add(buildKpiCard("Obras em execução",   lblObrasValor,      lblObrasSubtitulo,      UIConstants.COLOR_INFO,    new Color(59, 130, 246)));
        grid.add(buildKpiCard("Tarefas atrasadas",   lblTarefasValor,    lblTarefasSubtitulo,    UIConstants.COLOR_DANGER,  new Color(220, 38, 38)));
        grid.add(buildKpiCard("Valor por receber",   lblFaturasValor,    lblFaturasSubtitulo,    UIConstants.COLOR_WARNING, new Color(217, 119, 6)));
        grid.add(buildKpiCard("Orçamentos p/ aprovar", lblOrcamentosValor, lblOrcamentosSubtitulo, UIConstants.COLOR_WARNING, new Color(22, 163, 74)));

        return grid;
    }

    private JPanel buildKpiCard(String titulo, JLabel lblValor, JLabel lblSub, Color corValor, Color corDot) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        // Topo: dot + título
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topo.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(corDot);
        dot.setFont(dot.getFont().deriveFont(9f));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.PLAIN, 12f));
        lblTit.setForeground(UIManager.getColor("Label.disabledForeground"));

        topo.add(dot);
        topo.add(lblTit);

        // Valor grande
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 34f));
        lblValor.setForeground(corValor);

        // Subtítulo (ex: "3 com visita pendente")
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

    // ─────────────────────────────────────────────────────────────────────────
    //  TABELAS (obras + tarefas)
    // ─────────────────────────────────────────────────────────────────────────

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
        modeloObras = new DefaultTableModel(new String[]{"Obra", "Cliente", "Início"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = miniTabela(modeloObras);
        return buildCartaoTabela("Obras em execução", lblBadgeObras, tabela);
    }

    private JPanel buildTabelaTarefas() {
        modeloTarefas = new DefaultTableModel(new String[]{"Tarefa", "Funcionário", "Limite"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = miniTabela(modeloTarefas);

        // Coluna "Limite" a vermelho
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

        // Cabeçalho com badge
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 13f));
        header.add(lblTit,  BorderLayout.WEST);
        header.add(badge,   BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIManager.getColor("Panel.background"));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SECÇÃO INFERIOR (orçamentos + visitas + problemas)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildSecaoInferior() {
        JPanel linha = new JPanel(new GridLayout(1, 3, 16, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        painelOrcamentos = buildCardOrcamentos();
        painelVisitas    = buildCardVisitas();
        painelProblemas  = buildCardProblemas();

        linha.add(painelOrcamentos);
        linha.add(painelVisitas);
        linha.add(painelProblemas);
        return linha;
    }

    // ── Card: Orçamentos p/ aprovar ──────────────────────────────────────────

    private JPanel buildCardOrcamentos() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel tit = new JLabel("Orçamentos p/ aprovar");
        tit.setFont(tit.getFont().deriveFont(Font.BOLD, 13f));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(tit, BorderLayout.WEST);

        JLabel badge = buildBadge("2 pendentes", UIConstants.COLOR_WARNING);
        header.add(badge, BorderLayout.EAST);

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setName("listaOrcamentos");

        card.add(header, BorderLayout.NORTH);
        card.add(lista,  BorderLayout.CENTER);
        return card;
    }

    // ── Card: Visitas hoje e amanhã ──────────────────────────────────────────

    private JPanel buildCardVisitas() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel tit = new JLabel("Visitas hoje e amanhã");
        tit.setFont(tit.getFont().deriveFont(Font.BOLD, 13f));

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setName("listaVisitas");

        card.add(tit,   BorderLayout.NORTH);
        card.add(lista, BorderLayout.CENTER);
        return card;
    }

    // ── Card: Problemas reportados ────────────────────────────────────────────

    private JPanel buildCardProblemas() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel tit = new JLabel("Problemas reportados");
        tit.setFont(tit.getFont().deriveFont(Font.BOLD, 13f));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(tit, BorderLayout.WEST);

        JLabel badge = buildBadge("1 crítico", UIConstants.COLOR_DANGER);
        header.add(badge, BorderLayout.EAST);

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setName("listaProblemas");

        card.add(header, BorderLayout.NORTH);
        card.add(lista,  BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARREGAR DADOS
    // ─────────────────────────────────────────────────────────────────────────

    private void carregar() {
        carregarKpis();
        carregarTabelaObras();
        carregarTabelaTarefas();
        carregarOrcamentos();
        carregarVisitas();
        carregarProblemas();
    }

    // ── KPIs ─────────────────────────────────────────────────────────────────

    private void carregarKpis() {
        // Obras em execução
        List<Obra> emExec = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .collect(Collectors.toList());
        lblObrasValor.setText(String.valueOf(emExec.size()));
        lblObrasSubtitulo.setText(" ");  // pode ser enriquecido futuramente

        // Tarefas atrasadas
        List<Tarefa> atrasadas = tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null) return true;
                    String est = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !est.contains("conclu") && !est.contains("cancel");
                })
                .collect(Collectors.toList());
        lblTarefasValor.setText(String.valueOf(atrasadas.size()));
        if (!atrasadas.isEmpty()) {
            lblTarefasSubtitulo.setText("↑ " + atrasadas.size() + " tarefas em atraso");
        } else {
            lblTarefasSubtitulo.setText(" ");
        }

        // Valor por receber (faturas pendentes)
        List<Fatura> pendentes = faturaService.listarTodos().stream()
                .filter(f -> f.getValorTotalComIva() != null && f.getValorPago() != null
                        && f.getValorPago().compareTo(f.getValorTotalComIva()) < 0)
                .collect(Collectors.toList());

        BigDecimal totalPorReceber = pendentes.stream()
                .map(f -> f.getValorTotalComIva().subtract(f.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Formatar valor: se >= 1000 mostrar com ponto; ex: € 6.840
        String valorStr;
        long valorLong = totalPorReceber.longValue();
        if (valorLong >= 1000) {
            valorStr = "€ " + String.format("%,d", valorLong).replace(",", ".");
        } else {
            valorStr = "€ " + valorLong;
        }
        lblFaturasValor.setText(valorStr);
        lblFaturasSubtitulo.setText(pendentes.size() + " faturas pendentes");

        // Orçamentos p/ aprovar — usando faturas sem pagamento como proxy até módulo dedicado
        long orcPendentes = pendentes.size();
        lblOrcamentosValor.setText(String.valueOf(orcPendentes > 9 ? "9+" : orcPendentes));
        lblOrcamentosSubtitulo.setText("aguardam aprovação");
    }

    // ── Tabela obras ─────────────────────────────────────────────────────────

    private void carregarTabelaObras() {
        modeloObras.setRowCount(0);
        List<Obra> emExec = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
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
            modeloObras.addRow(new Object[]{"Nenhuma obra em execução", "", ""});
    }

    // ── Tabela tarefas ────────────────────────────────────────────────────────

    private void carregarTabelaTarefas() {
        modeloTarefas.setRowCount(0);
        List<Tarefa> atrasadas = tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null) return true;
                    String est = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !est.contains("conclu") && !est.contains("cancel");
                })
                .collect(Collectors.toList());

        lblBadgeTarefas.setText(atrasadas.size() + " em atraso");

        for (Tarefa t : atrasadas) {
            String func = t.getIdFuncionario() != null ? t.getIdFuncionario().getNome() : "—";
            String desc = t.getDescricao() != null && !t.getDescricao().isBlank()
                    ? truncar(t.getDescricao(), 32) : "#" + t.getId();
            String data = t.getDataLimite() != null ? t.getDataLimite().format(FMT_DATA) : "—";
            modeloTarefas.addRow(new Object[]{desc, func, data});
        }
        if (modeloTarefas.getRowCount() == 0)
            modeloTarefas.addRow(new Object[]{"Sem tarefas atrasadas", "", ""});
    }

    // ── Orçamentos ────────────────────────────────────────────────────────────

    private void carregarOrcamentos() {
        JPanel lista = findNamed(painelOrcamentos, "listaOrcamentos");
        if (lista == null) return;
        lista.removeAll();

        // Usar faturas pendentes como proxy até existir OrcamentoService
        List<Fatura> pendentes = faturaService.listarTodos().stream()
                .filter(f -> f.getValorTotalComIva() != null && f.getValorPago() != null
                        && f.getValorPago().compareTo(f.getValorTotalComIva()) < 0)
                .limit(3)
                .collect(Collectors.toList());

        if (pendentes.isEmpty()) {
            JLabel vazio = new JLabel("Sem orçamentos pendentes");
            vazio.setFont(vazio.getFont().deriveFont(12f));
            vazio.setForeground(UIManager.getColor("Label.disabledForeground"));
            lista.add(vazio);
        } else {
            for (Fatura f : pendentes) {
                lista.add(buildLinhaOrcamento(f));
                lista.add(Box.createVerticalStrut(10));
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaOrcamento(Fatura f) {
        JPanel linha = new JPanel(new BorderLayout(8, 2));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        linha.setAlignmentX(LEFT_ALIGNMENT);

        JPanel texto = new JPanel(new GridLayout(2, 1, 0, 2));
        texto.setOpaque(false);

        String titulo = f.getIdObra() != null && f.getIdObra().getDescricao() != null
                ? truncar(f.getIdObra().getDescricao(), 28) : "Fatura #" + f.getId();
        String sub    = "emitido em " +
                (f.getDataEmissao() != null ? f.getDataEmissao().format(FMT_FULL) : "—");

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 11f));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        texto.add(lblTit);
        texto.add(lblSub);

        JButton btnVer = buildSmallButton("Ver ↗");
        btnVer.setPreferredSize(new Dimension(56, 36));

        linha.add(texto,  BorderLayout.CENTER);
        linha.add(btnVer, BorderLayout.EAST);
        return linha;
    }

    // ── Visitas ───────────────────────────────────────────────────────────────

    private void carregarVisitas() {
        JPanel lista = findNamed(painelVisitas, "listaVisitas");
        if (lista == null) return;
        lista.removeAll();

        // Usar obras com visita pendente como proxy até existir VisitaService
        // Mostra obras em execução com datas próximas como "visitas"
        LocalDate hoje   = LocalDate.now();
        LocalDate amanha = hoje.plusDays(1);

        List<Obra> obrasProximas = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .limit(4)
                .collect(Collectors.toList());

        if (obrasProximas.isEmpty()) {
            JLabel vazio = new JLabel("Sem visitas agendadas");
            vazio.setFont(vazio.getFont().deriveFont(12f));
            vazio.setForeground(UIManager.getColor("Label.disabledForeground"));
            lista.add(vazio);
        } else {
            String[] horas = {"09:00", "14:30", "10:00", "11:00"};
            String[] dias  = {"hoje", "hoje", "amanhã", "amanhã"};
            Color[]  cores = {UIConstants.COLOR_INFO, UIConstants.COLOR_INFO,
                    new Color(150, 150, 150), UIConstants.COLOR_SUCCESS};

            for (int i = 0; i < obrasProximas.size(); i++) {
                Obra o = obrasProximas.get(i);
                lista.add(buildLinhaVisita(o, horas[i % horas.length],
                        dias[i % dias.length], cores[i % cores.length]));
                lista.add(separador());
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaVisita(Obra o, String hora, String dia, Color corDot) {
        JPanel linha = new JPanel(new BorderLayout(8, 0));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        linha.setAlignmentX(LEFT_ALIGNMENT);

        JLabel dot = new JLabel("●");
        dot.setForeground(corDot);
        dot.setFont(dot.getFont().deriveFont(10f));
        dot.setVerticalAlignment(SwingConstants.TOP);

        JPanel texto = new JPanel(new GridLayout(2, 1, 0, 2));
        texto.setOpaque(false);

        String titulo = o.getDescricao() != null ? truncar(o.getDescricao(), 30) : "#" + o.getId();
        String cliente = o.getIdCliente() != null ? o.getIdCliente().getNome() : "—";

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel lblSub = new JLabel(cliente + " · " + dia);
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 11f));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        texto.add(lblTit);
        texto.add(lblSub);

        JLabel lblHora = new JLabel(hora);
        lblHora.setFont(lblHora.getFont().deriveFont(Font.PLAIN, 12f));
        lblHora.setForeground(UIManager.getColor("Label.disabledForeground"));
        lblHora.setVerticalAlignment(SwingConstants.TOP);

        linha.add(dot,     BorderLayout.WEST);
        linha.add(texto,   BorderLayout.CENTER);
        linha.add(lblHora, BorderLayout.EAST);
        return linha;
    }

    // ── Problemas ─────────────────────────────────────────────────────────────

    private void carregarProblemas() {
        JPanel lista = findNamed(painelProblemas, "listaProblemas");
        if (lista == null) return;
        lista.removeAll();

        List<Problema> problemas = problemaService.listarTodos().stream()
                .limit(4)
                .collect(Collectors.toList());

        // Actualizar badge no header
        JPanel header = (JPanel) painelProblemas.getComponent(0);
        long criticos = problemas.stream()
                .filter(p -> p.getIdGravidade() != null &&
                        p.getIdGravidade().getNomeGravidade().toLowerCase().contains("crít"))
                .count();
        if (header.getComponentCount() > 1 && header.getComponent(1) instanceof JLabel) {
            JLabel badge = (JLabel) header.getComponent(1);
            badge.setText(criticos > 0 ? criticos + " crítico" + (criticos > 1 ? "s" : "") : problemas.size() + " abertos");
        }

        if (problemas.isEmpty()) {
            JLabel vazio = new JLabel("Sem problemas reportados");
            vazio.setFont(vazio.getFont().deriveFont(12f));
            vazio.setForeground(UIManager.getColor("Label.disabledForeground"));
            lista.add(vazio);
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
        JPanel linha = new JPanel(new BorderLayout(8, 2));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        linha.setAlignmentX(LEFT_ALIGNMENT);

        JPanel texto = new JPanel(new GridLayout(2, 1, 0, 2));
        texto.setOpaque(false);

        String descricao = p.getDescricao() != null ? truncar(p.getDescricao(), 28) : "#" + p.getId();
        String obraDesc  = (p.getIdObra() != null && p.getIdObra().getDescricao() != null)
                ? truncar(p.getIdObra().getDescricao(), 28) : "—";

        JLabel lblDesc  = new JLabel(descricao);
        lblDesc.setFont(lblDesc.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel lblObra  = new JLabel(obraDesc);
        lblObra.setFont(lblObra.getFont().deriveFont(Font.PLAIN, 11f));
        lblObra.setForeground(UIManager.getColor("Label.disabledForeground"));

        texto.add(lblDesc);
        texto.add(lblObra);

        // Badge de gravidade
        String nomeGrav = p.getIdGravidade() != null ? p.getIdGravidade().getNomeGravidade() : "—";
        Color corGrav   = gravColor(nomeGrav);
        JLabel badge    = buildBadge(nomeGrav, corGrav);

        linha.add(texto, BorderLayout.CENTER);
        linha.add(badge, BorderLayout.EAST);
        return linha;
    }

    private Color gravColor(String nome) {
        if (nome == null) return UIConstants.COLOR_INFO;
        String n = nome.toLowerCase();
        if (n.contains("crít")) return UIConstants.COLOR_DANGER;
        if (n.contains("alt"))  return UIConstants.COLOR_WARNING;
        return new Color(100, 116, 139); // média/baixa — cinza
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITÁRIOS VISUAIS
    // ─────────────────────────────────────────────────────────────────────────

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

    /** Procura um JPanel filho pelo nome. */
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

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS GERAIS
    // ─────────────────────────────────────────────────────────────────────────

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