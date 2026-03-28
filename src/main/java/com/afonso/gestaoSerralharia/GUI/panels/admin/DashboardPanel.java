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

public class DashboardPanel extends BasePanel {

    private final ObraService        obraService;
    private final TarefaService      tarefaService;
    private final FaturaService      faturaService;
    private final ProblemaService    problemaService;
    private final ClienteService     clienteService;
    private final FuncionarioService funcionarioService;

    private JLabel lblObrasValor;
    private JLabel lblObrasSubtitulo;
    private JLabel lblTarefasValor;
    private JLabel lblTarefasSubtitulo;
    private JLabel lblFaturasValor;
    private JLabel lblFaturasSubtitulo;
    private JLabel lblOrcamentosValor;
    private JLabel lblOrcamentosSubtitulo;

    private JLabel lblBadgeObras;
    private JLabel lblBadgeTarefas;

    private DefaultTableModel modeloObras;
    private DefaultTableModel modeloTarefas;

    private JPanel painelOrcamentos;
    private JPanel painelVisitas;
    private JPanel painelProblemas;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        JButton btnRefresh = buildButton("Actualizar");
        btnRefresh.addActionListener(e -> carregar());

        add(buildHeader("Dashboard", saudacao, btnRefresh), BorderLayout.NORTH);
        add(buildScroll(), BorderLayout.CENTER);
        carregar();
    }

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

        grid.add(buildKpiCard("Obras em execução",    lblObrasValor,      lblObrasSubtitulo,      UIConstants.COLOR_INFO,    new Color(59, 130, 246)));
        grid.add(buildKpiCard("Tarefas atrasadas",    lblTarefasValor,    lblTarefasSubtitulo,    UIConstants.COLOR_DANGER,  new Color(220, 38, 38)));
        grid.add(buildKpiCard("Valor por receber",    lblFaturasValor,    lblFaturasSubtitulo,    UIConstants.COLOR_WARNING, new Color(217, 119, 6)));
        grid.add(buildKpiCard("Orcamentos p/ aprovar", lblOrcamentosValor, lblOrcamentosSubtitulo, UIConstants.COLOR_WARNING, new Color(22, 163, 74)));

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
        centro.add(lblSub,   BorderLayout.SOUTH);

        card.add(topo,   BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);
        return card;
    }

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

    private JPanel buildCardOrcamentos() {
        JLabel badge = buildBadge("2 pendentes", UIConstants.COLOR_WARNING);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(cardTitulo("Orcamentos p/ aprovar"), BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        JPanel lista = listaPanel("listaOrcamentos");

        return buildCard(header, lista);
    }

    private JPanel buildCardVisitas() {
        JPanel lista = listaPanel("listaVisitas");
        return buildCard(cardTitulo("Visitas hoje e amanha"), lista);
    }

    private JPanel buildCardProblemas() {
        JLabel badge = buildBadge("1 critico", UIConstants.COLOR_DANGER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(cardTitulo("Problemas reportados"), BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        JPanel lista = listaPanel("listaProblemas");

        return buildCard(header, lista);
    }

    private JPanel buildCard(JComponent north, JPanel center) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor(), 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.add(north,  BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

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

    private void carregar() {
        carregarKpis();
        carregarTabelaObras();
        carregarTabelaTarefas();
        carregarOrcamentos();
        carregarVisitas();
        carregarProblemas();
    }

    private void carregarKpis() {
        List<Obra> emExec = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .collect(Collectors.toList());
        lblObrasValor.setText(String.valueOf(emExec.size()));
        lblObrasSubtitulo.setText(" ");

        List<Tarefa> atrasadas = tarefaService.buscarAtrasadas().stream()
                .filter(t -> {
                    if (t.getIdEstadoTarefa() == null) return true;
                    String est = t.getIdEstadoTarefa().getNomeEstado().toLowerCase();
                    return !est.contains("conclu") && !est.contains("cancel");
                })
                .collect(Collectors.toList());
        lblTarefasValor.setText(String.valueOf(atrasadas.size()));
        lblTarefasSubtitulo.setText(atrasadas.isEmpty() ? " " : atrasadas.size() + " tarefas em atraso");

        List<Fatura> pendentes = faturaService.listarTodos().stream()
                .filter(f -> f.getValorTotalComIva() != null && f.getValorPago() != null
                        && f.getValorPago().compareTo(f.getValorTotalComIva()) < 0)
                .collect(Collectors.toList());

        BigDecimal totalPorReceber = pendentes.stream()
                .map(f -> f.getValorTotalComIva().subtract(f.getValorPago()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long valorLong = totalPorReceber.longValue();
        String valorStr = valorLong >= 1000
                ? "€ " + String.format("%,d", valorLong).replace(",", ".")
                : "€ " + valorLong;
        lblFaturasValor.setText(valorStr);
        lblFaturasSubtitulo.setText(pendentes.size() + " faturas pendentes");

        long orcPendentes = pendentes.size();
        lblOrcamentosValor.setText(String.valueOf(orcPendentes > 9 ? "9+" : orcPendentes));
        lblOrcamentosSubtitulo.setText("aguardam aprovacao");
    }

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

    private void carregarOrcamentos() {
        JPanel lista = findNamed(painelOrcamentos, "listaOrcamentos");
        if (lista == null) return;
        lista.removeAll();

        List<Fatura> pendentes = faturaService.listarTodos().stream()
                .filter(f -> f.getValorTotalComIva() != null && f.getValorPago() != null
                        && f.getValorPago().compareTo(f.getValorTotalComIva()) < 0)
                .limit(3)
                .collect(Collectors.toList());

        if (pendentes.isEmpty()) {
            lista.add(labelVazio("Sem orcamentos pendentes"));
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
        String titulo = f.getIdObra() != null && f.getIdObra().getDescricao() != null
                ? truncar(f.getIdObra().getDescricao(), 28) : "Fatura #" + f.getId();
        String sub = "emitido em " +
                (f.getDataEmissao() != null ? f.getDataEmissao().format(FMT_FULL) : "—");

        JButton btnVer = buildSmallButton("Ver");
        btnVer.setPreferredSize(new Dimension(56, 36));

        JPanel linha = new JPanel(new BorderLayout(8, 2));
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.add(buildTexto(titulo, sub), BorderLayout.CENTER);
        linha.add(btnVer,                 BorderLayout.EAST);
        return linha;
    }

    private void carregarVisitas() {
        JPanel lista = findNamed(painelVisitas, "listaVisitas");
        if (lista == null) return;
        lista.removeAll();

        List<Obra> obras = obraService.listarTodos().stream()
                .filter(o -> o.getIdEstadoObra() != null &&
                        o.getIdEstadoObra().getNomeEstado().toLowerCase().contains("execu"))
                .limit(4)
                .collect(Collectors.toList());

        if (obras.isEmpty()) {
            lista.add(labelVazio("Sem visitas agendadas"));
        } else {
            String[] horas = {"09:00", "14:30", "10:00", "11:00"};
            String[] dias  = {"hoje", "hoje", "amanha", "amanha"};
            Color[]  cores = {UIConstants.COLOR_INFO, UIConstants.COLOR_INFO,
                    new Color(150, 150, 150), UIConstants.COLOR_SUCCESS};

            for (int i = 0; i < obras.size(); i++) {
                lista.add(buildLinhaVisita(obras.get(i), horas[i % horas.length],
                        dias[i % dias.length], cores[i % cores.length]));
                lista.add(separador());
            }
        }

        lista.revalidate();
        lista.repaint();
    }

    private JPanel buildLinhaVisita(Obra o, String hora, String dia, Color corDot) {
        String titulo  = o.getDescricao() != null ? truncar(o.getDescricao(), 30) : "#" + o.getId();
        String cliente = o.getIdCliente() != null ? o.getIdCliente().getNome() : "—";

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
        linha.add(dot,                           BorderLayout.WEST);
        linha.add(buildTexto(titulo, cliente + " · " + dia), BorderLayout.CENTER);
        linha.add(lblHora,                       BorderLayout.EAST);
        return linha;
    }

    private void carregarProblemas() {
        JPanel lista = findNamed(painelProblemas, "listaProblemas");
        if (lista == null) return;
        lista.removeAll();

        List<Problema> problemas = problemaService.listarTodos().stream()
                .limit(4)
                .collect(Collectors.toList());

        JPanel header = (JPanel) painelProblemas.getComponent(0);
        long criticos = problemas.stream()
                .filter(p -> p.getIdGravidade() != null &&
                        p.getIdGravidade().getNomeGravidade().toLowerCase().contains("crit"))
                .count();
        if (header.getComponentCount() > 1 && header.getComponent(1) instanceof JLabel) {
            JLabel badge = (JLabel) header.getComponent(1);
            badge.setText(criticos > 0
                    ? criticos + " critico" + (criticos > 1 ? "s" : "")
                    : problemas.size() + " abertos");
        }

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