package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Cliente;
import com.afonso.gestaoSerralharia.models.Estadoobra;
import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Linhaorcamento;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EstadoobraRepository;
import com.afonso.gestaoSerralharia.repositories.FaturaRepository;
import com.afonso.gestaoSerralharia.repositories.LinhaorcamentoRepository;
import com.afonso.gestaoSerralharia.repositories.ObraRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraService {

    private final ObraRepository obraRepository;
    private final EstadoobraRepository estadoobraRepository;
    private final FaturaRepository faturaRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final LinhaorcamentoRepository linhaorcamentoRepository;
    private final MaterialService materialService;
    private final MovimentofinanceiroService movimentofinanceiroService;

    public List<Obra> listarTodos() {
        return obraRepository.findAll();
    }

    public Obra buscarPorId(Integer id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Obra não encontrada: " + id));
    }

    public List<Obra> buscarPorCliente(Cliente cliente) {
        return obraRepository.findByIdCliente(cliente);
    }

    public List<Obra> buscarPorEstado(Estadoobra estado) {
        return obraRepository.findByIdEstadoObra(estado);
    }

    public Obra guardar(Obra obra) {
        if (obra.getRua() == null || obra.getRua().isBlank())
            throw new IllegalArgumentException("Rua da obra é obrigatória");
        if (obra.getLocalidade() == null || obra.getLocalidade().isBlank())
            throw new IllegalArgumentException("Localidade da obra é obrigatória");
        if (obra.getIdCliente() == null)
            throw new IllegalArgumentException("A obra tem de ter um cliente associado");
        if (obra.getIdCodpostal() == null)
            throw new IllegalArgumentException("O código postal é obrigatório");
        return obraRepository.save(obra);
    }

    public Obra atualizarEstado(Integer idObra, Integer idEstado) {
        Obra obra = buscarPorId(idObra);
        Estadoobra estado = estadoobraRepository.findById(idEstado)
                .orElseThrow(() -> new RuntimeException("Estado não encontrado: " + idEstado));

        switch (idEstado) {
            case 2 -> {if(obra.getDataInicio()==null) {obra.setDataInicio(LocalDate.now());}}
            case 3 -> {
                // Aproveita-se a logica de finalizar
                return finalizar(idObra);
            }
        }

        obra.setIdEstadoObra(estado);
        return obraRepository.save(obra);
    }

    public Obra finalizar(Integer idObra) {
        Obra obra = buscarPorId(idObra);

        String estadoAtual = obra.getIdEstadoObra().getNomeEstado().toLowerCase();
        if (estadoAtual.equals("concluída") || estadoAtual.equals("cancelada") || estadoAtual.equals("arquivada"))
            throw new IllegalStateException("Obra já está em estado '" + obra.getIdEstadoObra().getNomeEstado() + "'");

        List<Fatura> faturasPorPagar = faturaRepository.findByIdObraAndValorPagoLessThanValorTotalComIva(obra);
        if (!faturasPorPagar.isEmpty())
            throw new IllegalStateException("A obra tem faturas por pagar");

        consumirReservasDaObra(obra);

        Estadoobra estadoConcluida = estadoobraRepository.findByNomeEstadoIgnoreCase("Concluída")
                .orElseThrow(() -> new RuntimeException("Estado 'Concluída' não encontrado na BD"));
        obra.setIdEstadoObra(estadoConcluida);

        if(obra.getDataFim() == null) {obra.setDataFim(LocalDate.now());}

        return obraRepository.save(obra);
    }

    public void eliminar(Integer id) {
        buscarPorId(id);
        obraRepository.deleteById(id);
    }

    private void consumirReservasDaObra(Obra obra) {
        Orcamento orcamentoAprovado = orcamentoRepository
                .findFirstByIdObraAndAprovadoTrueOrderByVersaoDesc(obra)
                .orElse(null);
        if (orcamentoAprovado == null) return;

        for (Linhaorcamento linha : linhaorcamentoRepository.findByIdOrcamento(orcamentoAprovado)) {
            if (linha.getIdMaterial() == null) continue;
            BigDecimal reservado = linha.getQuantidadeReservada() != null ? linha.getQuantidadeReservada() : BigDecimal.ZERO;
            if (reservado.signum() <= 0) continue;

            materialService.consumirReserva(linha.getIdMaterial(), reservado);
            if (linha.getPrecoUnit() != null) {
                movimentofinanceiroService.registar(
                        LocalDate.now(),
                        "PERDA",
                        "MATERIAL",
                        "Consumo material '" + linha.getIdMaterial().getNome() + "' na obra #" + obra.getId(),
                        linha.getPrecoUnit().multiply(reservado).negate()
                );
            }
            linha.setQuantidadeReservada(BigDecimal.ZERO);
            linhaorcamentoRepository.save(linha);
        }
    }
}
