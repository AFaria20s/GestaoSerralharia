package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.util.CalculosFinanceiros;
import com.afonso.gestaoSerralharia.util.ResumoIva;
import com.afonso.gestaoSerralharia.util.TotaisFinanceiros;
import com.afonso.gestaoSerralharia.models.Estadoobra;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EstadoobraRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.ObraRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final ObraRepository obraRepository;
    private final EstadoobraRepository estadoobraRepository;
    private final LinhaorcamentoRepository linhaorcamentoRepository;
    private final MaterialService materialService;

    public List<Orcamento> listarTodos() {
        return orcamentoRepository.findAll();
    }

    public Orcamento buscarPorId(Integer id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado: " + id));
    }

    public Optional<Orcamento> buscarPorObra(Obra obra) {
        return orcamentoRepository.findFirstByIdObraAndAtivoTrueOrderByVersaoDesc(obra);
    }

    public Optional<Orcamento> buscarAprovadoPorObra(Obra obra) {
        return orcamentoRepository.findFirstByIdObraAndAprovadoTrueOrderByVersaoDesc(obra);
    }

    public List<Orcamento> listarPorObra(Obra obra) {
        return orcamentoRepository.findByIdObraOrderByVersaoDesc(obra);
    }

    public List<Orcamento> buscarAprovados() {
        return orcamentoRepository.findByAprovado(true);
    }

    public Orcamento guardar(Orcamento orcamento) {
        if (orcamento.getIdObra() == null)
            throw new IllegalArgumentException("O orçamento tem de estar associado a uma obra");
        if (orcamento.getDataEmissao() == null)
            orcamento.setDataEmissao(LocalDate.now());
        if (orcamento.getVersao() == null || orcamento.getVersao() <= 0)
            orcamento.setVersao(1);
        if (orcamento.getAtivo() == null)
            orcamento.setAtivo(true);
        if (orcamento.getAprovado() == null)
            orcamento.setAprovado(false);
        if (orcamento.getPrazoValidadeDias() == null || orcamento.getPrazoValidadeDias() <= 0)
            orcamento.setPrazoValidadeDias(30);
        if (orcamento.getPrazoPagamentoDias() == null || orcamento.getPrazoPagamentoDias() <= 0)
            orcamento.setPrazoPagamentoDias(30);
        if (orcamento.getObservacoesFinanceiras() != null)
            orcamento.setObservacoesFinanceiras(orcamento.getObservacoesFinanceiras().trim());
        if (orcamento.getId() == null) {
            boolean jaExisteAtivo = orcamentoRepository.existsByIdObraAndAtivoTrue(orcamento.getIdObra());
            if (jaExisteAtivo) {
                throw new IllegalStateException("Esta obra já tem um orçamento ativo. Crie uma revisão do orçamento atual.");
            }
        }
        return orcamentoRepository.save(orcamento);
    }

    public Orcamento aprovar(Integer idOrcamento) {
        Orcamento orcamento = buscarPorId(idOrcamento);
        if (orcamento.getAprovado())
            throw new IllegalStateException("Este orçamento já foi aprovado");
        if (linhaorcamentoRepository.findByIdOrcamento(orcamento).isEmpty())
            throw new IllegalStateException("Não é possível aprovar um orçamento sem linhas");
        Optional<Orcamento> anteriorAprovadoOpt = buscarAprovadoPorObra(orcamento.getIdObra())
                .filter(o -> !o.getId().equals(orcamento.getId()));
        if (anteriorAprovadoOpt.isPresent()) {
            Orcamento anteriorAprovado = anteriorAprovadoOpt.get();
            Integer versaoAtual = orcamento.getVersao() != null ? orcamento.getVersao() : 0;
            Integer versaoAprovada = anteriorAprovado.getVersao() != null ? anteriorAprovado.getVersao() : 0;
            if (versaoAprovada > versaoAtual) {
                throw new IllegalStateException(
                        "Não é possível aprovar uma versão antiga quando já existe uma versão mais recente aprovada");
            }
            libertarReservas(anteriorAprovado);
            anteriorAprovado.setAprovado(false);
            anteriorAprovado.setAtivo(false);
            orcamentoRepository.save(anteriorAprovado);
        }

        orcamento.setAprovado(true);
        orcamento.setAtivo(true);
        Estadoobra emExecucao = estadoobraRepository.findByNomeEstadoIgnoreCase("Em Execução")
                .orElseThrow(() -> new RuntimeException("Estado 'Em Execução' não encontrado na BD"));
        Obra obra = orcamento.getIdObra();
        obra.setIdEstadoObra(emExecucao);
        obraRepository.save(obra);
        Orcamento aprovado = orcamentoRepository.save(orcamento);
        reservarMateriais(aprovado);
        return aprovado;
    }

    public Orcamento criarRevisao(Integer idOrcamentoBase) {
        Orcamento base = buscarPorId(idOrcamentoBase);
        List<Orcamento> historico = listarPorObra(base.getIdObra());
        int versaoSeguinte = historico.stream()
                .map(Orcamento::getVersao)
                .filter(v -> v != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        historico.stream()
                .filter(o -> Boolean.TRUE.equals(o.getAtivo()))
                .forEach(o -> {
                    o.setAtivo(false);
                    orcamentoRepository.save(o);
                });

        Orcamento revisao = new Orcamento();
        revisao.setIdObra(base.getIdObra());
        revisao.setDataEmissao(LocalDate.now());
        revisao.setAprovado(false);
        revisao.setAtivo(true);
        revisao.setVersao(versaoSeguinte);
        revisao.setIdOrcamentoOrigem(base);
        revisao.setPrazoValidadeDias(base.getPrazoValidadeDias());
        revisao.setPrazoPagamentoDias(base.getPrazoPagamentoDias());
        revisao.setObservacoesFinanceiras(base.getObservacoesFinanceiras());
        revisao = orcamentoRepository.save(revisao);

        List<Linhaorcamento> novasLinhas = new ArrayList<>();
        for (Linhaorcamento linhaBase : linhaorcamentoRepository.findByIdOrcamento(base)) {
            Linhaorcamento copia = new Linhaorcamento();
            copia.setIdOrcamento(revisao);
            copia.setIdMaterial(linhaBase.getIdMaterial());
            copia.setIdTipoLinhaorcamento(linhaBase.getIdTipoLinhaorcamento());
            copia.setIdIva(linhaBase.getIdIva());
            copia.setIvaPercentagemAplicada(linhaBase.getIvaPercentagemAplicada());
            copia.setQuantidade(linhaBase.getQuantidade());
            copia.setPrecoUnit(linhaBase.getPrecoUnit());
            copia.setDescontoPercentagem(linhaBase.getDescontoPercentagem());
            copia.setNome(linhaBase.getNome());
            copia.setQuantidadeReservada(BigDecimal.ZERO);
            novasLinhas.add(linhaorcamentoRepository.save(copia));
        }
        return revisao;
    }

    public void eliminar(Integer id) {
        Orcamento orcamento = buscarPorId(id);
        if (orcamento.getAprovado())
            throw new IllegalStateException("Não é possível eliminar um orçamento aprovado");
        orcamentoRepository.deleteById(id);
    }

    private void reservarMateriais(Orcamento orcamento) {
        for (Linhaorcamento linha : linhaorcamentoRepository.findByIdOrcamento(orcamento)) {
            if (linha.getIdMaterial() == null || linha.getQuantidade() == null || linha.getQuantidade().signum() <= 0)
                continue;
            BigDecimal jaReservado = linha.getQuantidadeReservada() != null ? linha.getQuantidadeReservada() : BigDecimal.ZERO;
            BigDecimal faltaReservar = linha.getQuantidade().subtract(jaReservado);
            if (faltaReservar.signum() <= 0) continue;
            materialService.reservar(linha.getIdMaterial(), faltaReservar);
            linha.setQuantidadeReservada(linha.getQuantidade());
            linhaorcamentoRepository.save(linha);
        }
    }

    private void libertarReservas(Orcamento orcamento) {
        for (Linhaorcamento linha : linhaorcamentoRepository.findByIdOrcamento(orcamento)) {
            if (linha.getIdMaterial() == null) continue;
            BigDecimal reservado = linha.getQuantidadeReservada() != null ? linha.getQuantidadeReservada() : BigDecimal.ZERO;
            if (reservado.signum() <= 0) continue;
            materialService.libertarReserva(linha.getIdMaterial(), reservado);
            linha.setQuantidadeReservada(BigDecimal.ZERO);
            linhaorcamentoRepository.save(linha);
        }
    }

    public TotaisFinanceiros calcularTotais(Orcamento orcamento) {
        return CalculosFinanceiros.totaisLinhas(linhaorcamentoRepository.findByIdOrcamento(orcamento));
    }

    public List<ResumoIva> calcularResumoIva(Orcamento orcamento) {
        return CalculosFinanceiros.resumoIva(linhaorcamentoRepository.findByIdOrcamento(orcamento));
    }

    public LocalDate calcularDataValidade(Orcamento orcamento) {
        if (orcamento == null || orcamento.getDataEmissao() == null) return null;
        int prazo = orcamento.getPrazoValidadeDias() != null && orcamento.getPrazoValidadeDias() > 0
                ? orcamento.getPrazoValidadeDias()
                : 30;
        return orcamento.getDataEmissao().plusDays(prazo);
    }
}
