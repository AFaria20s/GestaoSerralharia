package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.FinanceiroSeriePonto;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.models.Tarefa;
import com.afonso.gestaoSerralharia.services.ClienteService;
import com.afonso.gestaoSerralharia.services.FaturaService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import com.afonso.gestaoSerralharia.services.ObraService;
import com.afonso.gestaoSerralharia.services.OrcamentoService;
import com.afonso.gestaoSerralharia.services.ProblemaService;
import com.afonso.gestaoSerralharia.services.TarefaService;
import com.afonso.gestaoSerralharia.services.VisitaService;
import com.afonso.gestaoSerralharia.services.MovimentofinanceiroService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardPanel extends BasePanel {

    private static final Locale LOCALE_PT = new Locale("pt", "PT");

    private final ObraService obraService;
    private final TarefaService tarefaService;
    private final FaturaService faturaService;
    private final OrcamentoService orcamentoService;
    private final MovimentofinanceiroService movimentofinanceiroService;

    private JLabel lblObrasValor;
    private JLabel lblObrasSubtitulo;
    private JLabel lblTarefasValor;
    private JLabel lblTarefasSubtitulo;
    private JLabel lblFaturasValor;
    private JLabel lblFaturasSubtitulo;
    private JLabel lblOrcamentosValor;
    private JLabel lblOrcamentosSubtitulo;
    private FinanceiroChartPanel financeiroChartPanel;
    private JComboBox<String> filtroPeriodoFinanceiro;

    public DashboardPanel(ObraService obraService, TarefaService tarefaService,
                          FaturaService faturaService, ProblemaService problemaService,
                          ClienteService clienteService, FuncionarioService funcionarioService,
                          OrcamentoService orcamentoService, VisitaService visitaService,
                          MovimentofinanceiroService movimentofinanceiroService) {
        this.obraService      = obraService;
        this.tarefaService    = tarefaService;
        this.faturaService    = faturaService;
        this.orcamentoService = orcamentoService;
        this.movimentofinanceiroService = movimentofinanceiroService;

        String nome = SessionManager.getInstance().getNome();
        String saudacao = saudacao() + (nome != null && !nome.isBlank() ? ", " + nome.split(" ")[0] : "");

        add(buildHeader("Dashboard", saudacao), BorderLayout.NORTH);
        add(buildScroll(), BorderLayout.CENTER);
        carregar();
    }

    private JScrollPane buildScroll() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(0, 0, 16, 0));

        corpo.add(buildSecaoKpi());
        corpo.add(Box.createVerticalStrut(16));
        corpo.add(buildSecaoFinanceira());

        JScrollPane scroll = new JScrollPane(corpo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    private JPanel buildSecaoFinanceira() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel titulo = new JLabel("Ganhos vs perdas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(titulo, BorderLayout.WEST);

        filtroPeriodoFinanceiro = new JComboBox<>(new String[]{"30 dias", "90 dias", "180 dias", "365 dias", "Histórico completo"});
        filtroPeriodoFinanceiro.setSelectedItem("180 dias");
        filtroPeriodoFinanceiro.addActionListener(e -> carregarFinanceiro());
        JPanel filtroWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filtroWrap.setOpaque(false);
        filtroWrap.add(filtroPeriodoFinanceiro);
        topo.add(filtroWrap, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        financeiroChartPanel = new FinanceiroChartPanel();
        financeiroChartPanel.setPreferredSize(new Dimension(900, 240));
        card.add(financeiroChartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSecaoKpi() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        grid.setAlignmentX(LEFT_ALIGNMENT);

        lblObrasValor          = new JLabel("—");
        lblObrasSubtitulo      = new JLabel(" ");
        lblTarefasValor        = new JLabel("—");
        lblTarefasSubtitulo    = new JLabel(" ");
        lblOrcamentosValor     = new JLabel("—");
        lblOrcamentosSubtitulo = new JLabel(" ");
        lblFaturasValor        = new JLabel("—");
        lblFaturasSubtitulo    = new JLabel(" ");

        grid.add(buildKpiCard("Obras em execucao", lblObrasValor, lblObrasSubtitulo,
                UIConstants.COLOR_INFO, new Color(59, 130, 246)));
        grid.add(buildKpiCard("Tarefas atrasadas", lblTarefasValor, lblTarefasSubtitulo,
                UIConstants.COLOR_DANGER, new Color(220, 38, 38)));
        grid.add(buildKpiCard("Orcamentos pendentes", lblOrcamentosValor, lblOrcamentosSubtitulo,
                UIConstants.COLOR_SUCCESS, new Color(22, 163, 74)));
        grid.add(buildKpiCard("Valor por receber", lblFaturasValor, lblFaturasSubtitulo,
                UIConstants.COLOR_WARNING, new Color(217, 119, 6)));

        return grid;
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
        centro.add(lblSub, BorderLayout.SOUTH);

        card.add(topo, BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    private void carregar() {
        carregarKpis();
        carregarFinanceiro();
    }

    private void carregarFinanceiro() {
        int dias = diasFiltroFinanceiro();
        List<FinanceiroSeriePonto> serie = movimentofinanceiroService.serie(dias);
        financeiroChartPanel.setSerie(serie);
    }

    private int diasFiltroFinanceiro() {
        String v = filtroPeriodoFinanceiro != null ? (String) filtroPeriodoFinanceiro.getSelectedItem() : "180 dias";
        if (v == null) return 180;
        return switch (v) {
            case "30 dias" -> 30;
            case "90 dias" -> 90;
            case "180 dias" -> 180;
            case "365 dias" -> 365;
            case "Histórico completo" -> 0;
            default -> 180;
        };
    }

    private void carregarKpis() {
        List<Obra> obrasEmExecucao = obrasEmExecucao();
        lblObrasValor.setText(String.valueOf(obrasEmExecucao.size()));
        lblObrasSubtitulo.setText(obrasEmExecucao.isEmpty()
                ? "sem obras activas"
                : obrasEmExecucao.size() + " em curso");

        List<Tarefa> tarefasAtrasadas = tarefasAtrasadas();
        lblTarefasValor.setText(String.valueOf(tarefasAtrasadas.size()));
        lblTarefasSubtitulo.setText(tarefasAtrasadas.isEmpty()
                ? "sem atrasos criticos"
                : tarefasAtrasadas.size() + " para resolver");

        List<Orcamento> orcamentosPendentes = orcamentosPendentes();
        lblOrcamentosValor.setText(String.valueOf(orcamentosPendentes.size()));
        lblOrcamentosSubtitulo.setText(orcamentosPendentes.isEmpty()
                ? "sem aprovacoes pendentes"
                : orcamentosPendentes.size() + " aguardam decisao");

        List<Fatura> faturasPendentes = faturasPendentes();
        BigDecimal totalPorReceber = faturasPendentes.stream()
                .map(this::saldoPorReceber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblFaturasValor.setText(formatarEuro(totalPorReceber));
        lblFaturasSubtitulo.setText(faturasPendentes.isEmpty()
                ? "sem valores em aberto"
                : faturasPendentes.size() + " por liquidar");
    }

    private List<Obra> obrasEmExecucao() {
        return obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null)
                .filter(o -> o.getIdEstadoObra().getNomeEstado() != null)
                .filter(o -> o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .collect(Collectors.toList());
    }

    private List<Tarefa> tarefasAtrasadas() {
        return tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null || t.getIdEstadoTarefa().getNomeEstado() == null) {
                        return true;
                    }
                    String estado = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !estado.contains("conclu") && !estado.contains("cancel");
                })
                .collect(Collectors.toList());
    }

    private List<Orcamento> orcamentosPendentes() {
        return orcamentoService.listarTodos().stream()
                .filter(o -> o.getAprovado() != null && !o.getAprovado())
                .collect(Collectors.toList());
    }

    private List<Fatura> faturasPendentes() {
        return faturaService.listarTodos().stream()
                .filter(f -> saldoPorReceber(f).signum() > 0)
                .collect(Collectors.toList());
    }

    private BigDecimal saldoPorReceber(Fatura fatura) {
        if (fatura.getValorTotalComIva() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal valorPago = fatura.getValorPago() != null ? fatura.getValorPago() : BigDecimal.ZERO;
        BigDecimal saldo = fatura.getValorTotalComIva().subtract(valorPago);
        return saldo.signum() > 0 ? saldo : BigDecimal.ZERO;
    }

    private String formatarEuro(BigDecimal valor) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance(LOCALE_PT);
        formatador.setMaximumFractionDigits(0);
        formatador.setMinimumFractionDigits(0);
        return formatador.format(valor);
    }

    private Color borderColor() {
        Color cor = UIManager.getColor("Component.borderColor");
        return cor != null ? cor : new Color(226, 232, 240);
    }

    private static String saudacao() {
        int h = LocalTime.now().getHour();
        if (h < 12) return "Bom dia";
        if (h < 19) return "Boa tarde";
        return "Boa noite";
    }

    private static class FinanceiroChartPanel extends JPanel {
        private List<FinanceiroSeriePonto> serie = List.of();

        void setSerie(List<FinanceiroSeriePonto> novaSerie) {
            this.serie = novaSerie != null ? novaSerie : List.of();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int l = 42, r = 18, t = 16, b = 28;
                g2.setColor(new Color(148, 163, 184));
                g2.drawRect(l, t, w - l - r, h - t - b);

                if (serie.isEmpty()) {
                    g2.setColor(new Color(100, 116, 139));
                    g2.drawString("Sem movimentos financeiros no período.", l + 12, t + 24);
                    return;
                }

                java.math.BigDecimal maxAbs = java.math.BigDecimal.ONE;
                for (FinanceiroSeriePonto p : serie) {
                    java.math.BigDecimal s = p.getSaldo() != null ? p.getSaldo().abs() : java.math.BigDecimal.ZERO;
                    if (s.compareTo(maxAbs) > 0) maxAbs = s;
                }
                double max = maxAbs.doubleValue();
                int plotW = w - l - r;
                int plotH = h - t - b;
                int zeroY = t + plotH / 2;

                g2.setColor(new Color(203, 213, 225));
                g2.drawLine(l, zeroY, l + plotW, zeroY);

                int prevX = -1, prevY = -1;
                for (int i = 0; i < serie.size(); i++) {
                    FinanceiroSeriePonto p = serie.get(i);
                    double saldo = p.getSaldo() != null ? p.getSaldo().doubleValue() : 0d;
                    int x = l + (int) ((i / (double) Math.max(1, serie.size() - 1)) * plotW);
                    int y = zeroY - (int) ((saldo / max) * (plotH * 0.45));

                    g2.setColor(saldo >= 0 ? new Color(22, 163, 74) : new Color(220, 38, 38));
                    g2.fillOval(x - 2, y - 2, 4, 4);
                    if (prevX >= 0) {
                        g2.setColor(new Color(59, 130, 246));
                        g2.drawLine(prevX, prevY, x, y);
                    }
                    prevX = x;
                    prevY = y;
                }

                g2.setColor(new Color(71, 85, 105));
                g2.drawString("-" + maxAbs.setScale(0, java.math.RoundingMode.HALF_UP) + "€", 6, t + plotH - 2);
                g2.drawString("+" + maxAbs.setScale(0, java.math.RoundingMode.HALF_UP) + "€", 6, t + 10);
            } finally {
                g2.dispose();
            }
        }
    }
}
