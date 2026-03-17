package com.afonso.gestaoSerralharia;

import com.afonso.gestaoSerralharia.GUI.GUIDesktop;
import com.afonso.gestaoSerralharia.models.*;
import com.afonso.gestaoSerralharia.services.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;

/**
 * Aplicação de Gestão de Serralharia
 * Demonstração de acesso à base de dados atraves da camada BLL
 */
@SpringBootApplication
@RequiredArgsConstructor
public class GestaoSerralhariaApplication implements CommandLineRunner {

	private final ClienteService clienteService;
	private final MaterialService materialService;
	private final OrcamentoService orcamentoService;
	private final EncomendaService encomendaService;
	private final ObraService obraService;
	private final TarefaService tarefaService;
	private final LinhaorcamentoService linhaorcamentoService;
	private final FaturaService faturaService;

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(GestaoSerralhariaApplication.class);

		// Isto desativa o modo "headless" que causa a Exception
		builder.headless(false);
		builder.run(args);

		// Inicia a GUI
		SwingUtilities.invokeLater(GUIDesktop::new);
	}

	private void separador(String titulo) {
		System.out.println("\n--- " + titulo + " ---");
	}

	@Override
	@Transactional
	public void run(String[] args) {
		separador("CLIENTES");
		clienteService.listarTodos()
				.forEach(c -> System.out.println(c.getId() + " | " + c.getNome()));

		separador("OBRAS DO CLIENTE");
		Cliente cliente = clienteService.listarTodos().get(0);
		System.out.println("Cliente: " + cliente.getNome());
		obraService.buscarPorCliente(cliente)
				.forEach(o -> System.out.println(o.getId() + " | " + o.getDescricao() + " | " + o.getIdEstadoObra().getNomeEstado()));

		separador("ORÇAMENTO E LINHAS DA OBRA");
		Obra obra = obraService.buscarPorCliente(cliente).get(0);
		Orcamento orc = orcamentoService.buscarPorObra(obra).orElse(null);
		if (orc != null)
			linhaorcamentoService.buscarPorOrcamento(orc)
					.forEach(l -> System.out.println(l.getNome() + " | " + l.getQuantidade() + " x " + l.getPrecoUnit() + "€"));

		separador("TAREFAS ATRASADAS");
		tarefaService.buscarAtrasadas()
				.forEach(t -> System.out.println("Tarefa #" + t.getId() + " | Limite: " + t.getDataLimite()));

		separador("MATERIAIS COM STOCK BAIXO");
		materialService.buscarStockBaixo(50)
				.forEach(m -> System.out.println(m.getNome() + " | Stock: " + m.getStockAtual()));

		separador("ENCOMENDAS POR ENTREGAR");
		encomendaService.buscarPorEntregue(false)
				.forEach(e -> System.out.println("Encomenda #" + e.getId() + " | " + e.getIdFornecedor().getNome()));

		separador("FATURAS DA OBRA #1");
		faturaService.buscarPorObra(obra)
				.forEach(f -> System.out.println("Fatura #" + f.getId() + " | " + f.getValorTotalComIva() + "€ | Pago: " + f.getValorPago() + "€"));
	}
}