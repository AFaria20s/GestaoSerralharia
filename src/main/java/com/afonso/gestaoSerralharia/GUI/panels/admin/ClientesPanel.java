package com.afonso.gestaoSerralharia.GUI.panels.admin;

import com.afonso.gestaoSerralharia.GUI.UIConstants;
import com.afonso.gestaoSerralharia.GUI.panels.BasePanel;
import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.services.ClienteService;
import com.afonso.gestaoSerralharia.services.CodpostalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientesPanel extends BasePanel {

    private final ClienteService   clienteService;
    private final CodpostalService codpostalService;

    private JTextField        campoPesquisa;
    private DefaultTableModel modelo;
    private JTable            tabela;

    private static final String[] COLUNAS = {"ID", "Nome", "NIF", "Email", "Morada", "Cód. Postal"};

    public ClientesPanel(ClienteService clienteService, CodpostalService codpostalService) {
        this.clienteService   = clienteService;
        this.codpostalService = codpostalService;

        JButton btnNovo = buildButton("+ Novo Cliente");
        btnNovo.putClientProperty("JButton.buttonType", "roundRect");
        btnNovo.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.addActionListener(e -> abrirDialogoNovo());

        add(buildHeader("Clientes", "", btnNovo), BorderLayout.NORTH);
        add(buildCorpo(), BorderLayout.CENTER);
        carregarTabela(null);
    }

    private JPanel buildCorpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(buildBarraPesquisa(), BorderLayout.NORTH);
        panel.add(buildAreaTabela(),    BorderLayout.CENTER);
        return panel;
    }

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

        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);

        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(120);

        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDialogoEditar();
            }
        });

        return buildTablePane(tabela);
    }

    private void carregarTabela(String filtroNome) {
        modelo.setRowCount(0);
        List<Cliente> lista = filtroNome != null && !filtroNome.isBlank()
                ? clienteService.buscarPorNome(filtroNome)
                : clienteService.listarTodos();

        for (Cliente c : lista) {
            String morada = (c.getRua() != null ? c.getRua() : "") +
                    (c.getNporta() != null ? " " + c.getNporta() : "");
            String cp = c.getIdCodpostal() != null ? c.getIdCodpostal().getCodpostal() : "—";
            modelo.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getNif()   != null ? c.getNif()   : "—",
                    c.getEmail() != null ? c.getEmail() : "—",
                    morada.isBlank() ? "—" : morada.trim(),
                    cp
            });
        }
    }

    private void filtrar() {
        carregarTabela(campoPesquisa.getText().trim());
    }

    private Integer idSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(row), 0);
    }

    private void abrirDialogoNovo() {
        JDialog dlg = criarDialogo("Novo Cliente");
        FormCliente form = new FormCliente(dlg, null);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                clienteService.guardar(form.construirCliente());
                carregarTabela(campoPesquisa.getText().trim());
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    private void abrirDialogoEditar() {
        Integer id = idSelecionado();
        if (id == null) return;

        Cliente existente;
        try {
            existente = clienteService.buscarPorId(id);
        } catch (Exception ex) {
            mostrarErro(null, ex.getMessage());
            return;
        }

        JDialog dlg = criarDialogo("Editar Cliente");
        FormCliente form = new FormCliente(dlg, existente);
        dlg.add(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (form.confirmado()) {
            try {
                Cliente atualizado = form.construirCliente();
                atualizado.setId(existente.getId());
                clienteService.guardar(atualizado);
                carregarTabela(campoPesquisa.getText().trim());
            } catch (Exception ex) {
                mostrarErro(null, ex.getMessage());
            }
        }
    }

    public void eliminarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona um cliente na tabela primeiro.",
                    "Nenhuma selecção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tens a certeza que pretendes eliminar este cliente?\nEsta acção não pode ser desfeita.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                clienteService.eliminar(id);
                carregarTabela(campoPesquisa.getText().trim());
            } catch (Exception ex) {
                mostrarErro(null, "Não foi possível eliminar: " + ex.getMessage());
            }
        }
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

    private void mostrarErro(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : this,
                msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    //  FORMULÁRIO
    // =========================================================================

    private class FormCliente extends JPanel {

        private boolean confirmado = false;

        private final JTextField campoNome;
        private final JTextField campoNif;
        private final JTextField campoEmail;
        private final JTextField campoRua;
        private final JTextField campoNporta;
        private final JTextField campoCodpostal;
        private final JLabel     lblErro;

        FormCliente(JDialog dialogo, Cliente cliente) {
            setLayout(new BorderLayout(0, 0));
            setBorder(new EmptyBorder(24, 28, 20, 28));
            setPreferredSize(new Dimension(420, 390));

            JPanel corpo = new JPanel(new GridBagLayout());
            corpo.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            campoNome      = new JTextField();
            campoNif       = new JTextField();
            campoEmail     = new JTextField();
            campoRua       = new JTextField();
            campoNporta    = new JTextField();
            campoCodpostal = new JTextField();

            if (cliente != null) {
                campoNome.setText(cliente.getNome());
                campoNif.setText(cliente.getNif() != null ? cliente.getNif() : "");
                campoEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
                campoRua.setText(cliente.getRua() != null ? cliente.getRua() : "");
                campoNporta.setText(cliente.getNporta() != null ? cliente.getNporta() : "");
                if (cliente.getIdCodpostal() != null)
                    campoCodpostal.setText(cliente.getIdCodpostal().getCodpostal());
            }

            int row = 0;
            adicionarCampo(corpo, c, row++, "Nome *",        campoNome);
            adicionarCampo(corpo, c, row++, "NIF",           campoNif);
            adicionarCampo(corpo, c, row++, "Email",         campoEmail);
            adicionarCampo(corpo, c, row++, "Rua",           campoRua);
            adicionarCampo(corpo, c, row++, "Nº Porta",      campoNporta);
            adicionarCampo(corpo, c, row++, "Código Postal", campoCodpostal);

            lblErro = new JLabel(" ");
            lblErro.setForeground(UIConstants.COLOR_DANGER);
            lblErro.setFont(lblErro.getFont().deriveFont(UIConstants.FONT_SMALL));
            c.gridy = row * 2;
            c.gridx = 0;
            c.gridwidth = 2;
            c.insets = new Insets(6, 0, 0, 0);
            corpo.add(lblErro, c);

            add(corpo, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rodape.setOpaque(false);
            rodape.setBorder(new EmptyBorder(14, 0, 0, 0));

            JButton btnCancelar = buildButton("Cancelar");
            JButton btnGuardar  = buildButton(cliente == null ? "Criar" : "Guardar");
            btnGuardar.setBackground(UIConstants.COLOR_ADMIN_ACCENT);
            btnGuardar.setForeground(Color.WHITE);

            btnCancelar.addActionListener(e -> dialogo.dispose());
            btnGuardar.addActionListener(e -> {
                if (validar()) {
                    confirmado = true;
                    dialogo.dispose();
                }
            });

            rodape.add(btnCancelar);
            rodape.add(btnGuardar);
            add(rodape, BorderLayout.SOUTH);
        }

        private void adicionarCampo(JPanel panel, GridBagConstraints c,
                                    int row, String label, JComponent campo) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(UIConstants.FONT_FIELD_LABEL));

            c.gridy = row * 2;
            c.gridx = 0;
            c.gridwidth = 2;
            c.insets = new Insets(8, 0, 2, 0);
            panel.add(lbl, c);

            c.gridy = row * 2 + 1;
            c.insets = new Insets(0, 0, 4, 0);
            panel.add(campo, c);
        }

        private boolean validar() {
            if (campoNome.getText().trim().isBlank()) {
                lblErro.setText("O nome é obrigatório.");
                return false;
            }
            String nif = campoNif.getText().trim();
            if (!nif.isBlank() && nif.length() != 9) {
                lblErro.setText("O NIF deve ter exactamente 9 caracteres.");
                return false;
            }
            String email = campoEmail.getText().trim();
            if (!email.isBlank() && !email.contains("@")) {
                lblErro.setText("Introduz um email válido.");
                return false;
            }
            lblErro.setText(" ");
            return true;
        }

        boolean confirmado() { return confirmado; }

        Cliente construirCliente() {
            Cliente c = new Cliente();
            c.setNome(campoNome.getText().trim());

            String nif = campoNif.getText().trim();
            c.setNif(nif.isBlank() ? null : nif);

            String email = campoEmail.getText().trim();
            c.setEmail(email.isBlank() ? null : email);

            String rua = campoRua.getText().trim();
            c.setRua(rua.isBlank() ? null : rua);

            String nporta = campoNporta.getText().trim();
            c.setNporta(nporta.isBlank() ? null : nporta);

            String cp = campoCodpostal.getText().trim();
            if (!cp.isBlank())
                c.setIdCodpostal(codpostalService.encontrarOuCriar(cp));

            return c;
        }
    }
}
