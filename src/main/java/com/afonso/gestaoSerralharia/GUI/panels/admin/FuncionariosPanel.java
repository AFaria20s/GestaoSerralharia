package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Cargo;
import com.afonso.gestaoSerralharia.models.Funcionario;
import com.afonso.gestaoSerralharia.services.CargoService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel de gestão de funcionários (RF11 — criar, editar, remover).
 *
 * Funcionalidades:
 *  • Listar todos os funcionários com nome, email e cargo
 *  • Pesquisar por nome em tempo real
 *  • Criar novo funcionário (nome, email, password, cargo)
 *  • Editar dados de um funcionário existente
 *  • Eliminar funcionário (com confirmação)
 *
 * Integra com:
 *  – FuncionarioService  (CRUD de funcionários)
 *  – CargoService        (lista de cargos disponíveis)
 */
public class FuncionariosPanel extends BasePanel {

    // ── Serviços ─────────────────────────────────────────────────────────────
    private final FuncionarioService funcionarioService;
    private final CargoService       cargoService;

    // ── UI ───────────────────────────────────────────────────────────────────
    private JTextField      campoPesquisa;
    private DefaultTableModel modelo;
    private JTable          tabela;

    // Colunas da tabela
    private static final String[] COLUNAS = {"ID", "Nome", "Email", "Cargo"};

    // ─────────────────────────────────────────────────────────────────────────

    public FuncionariosPanel(FuncionarioService funcionarioService,
                             CargoService cargoService) {
        this.funcionarioService = funcionarioService;
        this.cargoService       = cargoService;

        // ── Botão "Novo Funcionário" no cabeçalho
        JButton btnNovo = buildButton("+ Novo Funcionário");
        btnNovo.putClientProperty("JButton.buttonType", "roundRect");
        btnNovo.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFocusPainted(false);
        btnNovo.addActionListener(e -> abrirDialogoNovo());

        add(buildHeader("Funcionários",
                        "RF11 · RF12 — gerir funcionários da empresa",
                        btnNovo),
                BorderLayout.NORTH);

        add(buildCorpo(), BorderLayout.CENTER);

        carregarTabela(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CORPO PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildBarraPesquisa(), BorderLayout.NORTH);
        panel.add(buildAreaTabela(),    BorderLayout.CENTER);
        return panel;
    }

    // ── Barra de pesquisa ────────────────────────────────────────────────────

    private JPanel buildBarraPesquisa() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);

        campoPesquisa = buildSearchField("Pesquisar por nome…");
        campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        bar.add(campoPesquisa);
        return bar;
    }

    // ── Tabela ───────────────────────────────────────────────────────────────

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

        // Ocultar coluna ID (necessária internamente mas desnecessária visualmente)
        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);

        // Larguras de colunas visíveis
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(140);

        // Painel de acções por linha ao seleccionar
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) atualizarBotoesAcao();
        });

        // Double-click para editar
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDialogoEditar();
            }
        });

        JScrollPane scroll = buildTablePane(tabela);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DADOS
    // ─────────────────────────────────────────────────────────────────────────

    private void carregarTabela(String filtroNome) {
        modelo.setRowCount(0);

        List<Funcionario> lista = filtroNome != null && !filtroNome.isBlank()
                ? funcionarioService.buscarPorNome(filtroNome)
                : funcionarioService.listarTodos();

        for (Funcionario f : lista) {
            String cargo = f.getIdCargo() != null ? f.getIdCargo().getNome() : "—";
            modelo.addRow(new Object[]{f.getId(), f.getNome(), f.getEmail(), cargo});
        }
    }

    private void filtrar() {
        carregarTabela(campoPesquisa.getText().trim());
    }

    /** Devolve o ID do funcionário na linha seleccionada, ou null. */
    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    private void atualizarBotoesAcao() {
        // reservado para eventual toolbar de acção contextual
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIÁLOGOS  –  NOVO
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDialogoNovo() {
        JDialog dlg = criarDialogo("Novo Funcionário");
        FormFuncionario form = new FormFuncionario(dlg, null);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                funcionarioService.guardar(form.construirFuncionario());
                carregarTabela(campoPesquisa.getText().trim());
                mostrarSucesso(dlg, "Funcionário criado com sucesso.");
            } catch (Exception ex) {
                mostrarErro(dlg, ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EDITAR
    // ─────────────────────────────────────────────────────────────────────────

    private void abrirDialogoEditar() {
        Integer id = idSelecionado();
        if (id == null) return;

        Funcionario existente;
        try {
            existente = funcionarioService.buscarPorId(id);
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
            return;
        }

        JDialog dlg = criarDialogo("Editar Funcionário");
        FormFuncionario form = new FormFuncionario(dlg, existente);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                Funcionario atualizado = form.construirFuncionario();
                atualizado.setId(existente.getId());
                if (atualizado.getPassword().isBlank()) {
                    atualizado.setPassword(existente.getPassword());
                }
                funcionarioService.guardar(atualizado);
                carregarTabela(campoPesquisa.getText().trim());
                mostrarSucesso(null, "Dados actualizados.");
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIÁLOGOS  –  ELIMINAR
    // ─────────────────────────────────────────────────────────────────────────

    /** Pode ser chamado a partir de um botão externo ou menu contextual. */
    public void eliminarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona um funcionário na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar este funcionário?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                funcionarioService.eliminar(id);
                carregarTabela(campoPesquisa.getText().trim());
            } catch (Exception ex) {
                mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
            }
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
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    //  FORMULÁRIOS
    // =========================================================================

    /**
     * Painel de formulário reutilizável para criar e editar funcionários.
     * Quando {@code funcionarioExistente} é null, opera em modo "Novo".
     */
    private class FormFuncionario extends JPanel {

        private boolean confirmado = false;

        // Campos
        private final JTextField   campoNome;
        private final JTextField   campoEmail;
        private final JPasswordField campoPassword;
        private final JComboBox<Cargo> comboCargo;
        private final JLabel       lblErro;

        FormFuncionario(JDialog dialogo, Funcionario funcionario) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(400, 300));

            // ── Corpo do formulário
            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(5, 0, 5, 0);
            c.weightx = 1.0;

            // Nome
            campoNome  = new JTextField();
            campoEmail = new JTextField();
            campoPassword = new JPasswordField();

            // Cargo
            List<Cargo> cargos = cargoService.listarTodos();
            comboCargo = new JComboBox<>(cargos.toArray(new Cargo[0]));
            comboCargo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object v,
                                                              int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, idx, sel, foc);
                    setText(v instanceof Cargo ? ((Cargo) v).getNome() : "—");
                    return this;
                }
            });

            // Preencher campos se edição
            if (funcionario != null) {
                campoNome.setText(funcionario.getNome());
                campoEmail.setText(funcionario.getEmail());
                // Password em branco = não alterar
                campoPassword.putClientProperty("JTextField.placeholderText",
                        "Deixa em branco para não alterar");
                if (funcionario.getIdCargo() != null) {
                    for (int i = 0; i < cargos.size(); i++) {
                        if (cargos.get(i).getId().equals(funcionario.getIdCargo().getId())) {
                            comboCargo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            // Adicionar linhas ao GridBag
            int row = 0;
            adicionarCampo(corpo, c, row++, "Nome *",      campoNome);
            adicionarCampo(corpo, c, row++, "Email *",     campoEmail);
            adicionarCampo(corpo, c, row++,
                    funcionario == null ? "Password *" : "Nova password",
                    campoPassword);
            adicionarCampo(corpo, c, row++, "Cargo",       comboCargo);

            // Erro
            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = row;
            c.gridx = 0;
            c.gridwidth = 2;
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            // ── Botões
            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 0, 0, 0));

            JButton btnCancelar = buildButton("Cancelar");
            JButton btnGuardar  = buildButton(funcionario == null ? "Criar" : "Guardar");
            btnGuardar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnGuardar.setForeground(Color.WHITE);

            btnCancelar.addActionListener(e -> dialogo.dispose());
            btnGuardar.addActionListener(e -> {
                if (validar(funcionario == null)) {
                    confirmado = true;
                    dialogo.dispose();
                }
            });

            rodape.add(btnCancelar);
            rodape.add(btnGuardar);
            add(rodape, BorderLayout.SOUTH);
        }

        // ── Helpers de layout ─────────────────────────────────────────────

        private void adicionarCampo(JPanel panel, GridBagConstraints c,
                                    int row, String label, JComponent campo) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));

            c.gridy = row;
            c.gridx = 0;
            c.gridwidth = 2;
            c.insets = new Insets(8, 0, 2, 0);
            panel.add(lbl, c);

            c.gridy = row;
            c.gridx = 0;
            c.gridwidth = 2;
            c.insets = new Insets(0, 0, 4, 0);
            // Usar row + 0.5 não é possível; usar dois rows por campo
            c.gridy = row * 2 + 1;
            panel.add(campo, c);
        }

        // ── Validação ────────────────────────────────────────────────────

        private boolean validar(boolean passwordObrigatoria) {
            String nome  = campoNome.getText().trim();
            String email = campoEmail.getText().trim();
            String pass  = new String(campoPassword.getPassword()).trim();

            if (nome.isBlank()) {
                lblErro.setText("O nome é obrigatório.");
                return false;
            }
            if (email.isBlank() || !email.contains("@")) {
                lblErro.setText("Introduz um email válido.");
                return false;
            }
            if (passwordObrigatoria && pass.isBlank()) {
                lblErro.setText("A password é obrigatória para um novo funcionário.");
                return false;
            }
            if (!pass.isBlank() && pass.length() < 6) {
                lblErro.setText("A password deve ter pelo menos 6 caracteres.");
                return false;
            }
            lblErro.setText(" ");
            return true;
        }

        // ── API pública ──────────────────────────────────────────────────

        boolean confirmado() { return confirmado; }

        Funcionario construirFuncionario() {
            Funcionario f = new Funcionario();
            f.setNome(campoNome.getText().trim());
            f.setEmail(campoEmail.getText().trim());
            f.setPassword(new String(campoPassword.getPassword()).trim());
            if (comboCargo.getSelectedItem() instanceof Cargo) {
                f.setIdCargo((Cargo) comboCargo.getSelectedItem());
            }
            return f;
        }
    }
}