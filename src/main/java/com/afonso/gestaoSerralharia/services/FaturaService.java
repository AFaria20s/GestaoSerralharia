package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.models.Fatura;
import com.afonso.gestaoSerralharia.models.Obra;
import com.afonso.gestaoSerralharia.models.Orcamento;
import com.afonso.gestaoSerralharia.repositories.EstadopagamentoRepository;
import com.afonso.gestaoSerralharia.repositories.FaturaRepository;
import com.afonso.gestaoSerralharia.repositories.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final EstadopagamentoRepository estadopagamentoRepository;

    public List<Fatura> listarTodos() {
        return faturaRepository.findAll();
    }

    public Fatura buscarPorId(Integer id) {
        return faturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
    }

    public List<Fatura> buscarPorObra(Obra obra) {
        return faturaRepository.findByIdObra(obra);
    }

    public Fatura guardar(Fatura fatura) {
        if (fatura.getIdObra() == null)
            throw new IllegalArgumentException("A fatura tem de estar associada a uma obra");
        Orcamento orcamento = orcamentoRepository.findByIdObra(fatura.getIdObra())
                .orElseThrow(() -> new IllegalStateException("Não existe orçamento aprovado para esta obra"));
        if (!orcamento.getAprovado())
            throw new IllegalStateException("Não é possível faturar uma obra sem orçamento aprovado");
        if (fatura.getValorTotalComIva() == null || fatura.getValorTotalComIva().signum() <= 0)
            throw new IllegalArgumentException("O valor total da fatura tem de ser maior que zero");
        return faturaRepository.save(fatura);
    }

    public Fatura registarPagamento(Integer idFatura, BigDecimal valorPago) {
        Fatura fatura = buscarPorId(idFatura);
        if (valorPago == null || valorPago.signum() <= 0)
            throw new IllegalArgumentException("O valor pago tem de ser maior que zero");
        if (valorPago.compareTo(fatura.getValorTotalComIva()) > 0)
            throw new IllegalArgumentException("O valor pago não pode ser superior ao valor total da fatura");
        fatura.setValorPago(fatura.getValorPago().add(valorPago));
        if (fatura.getValorPago().compareTo(fatura.getValorTotalComIva()) >= 0) {
            estadopagamentoRepository.findByNomeEstadoIgnoreCase("Pago")
                    .ifPresent(fatura::setIdEstadoPagamento);
        }
        return faturaRepository.save(fatura);
    }

    public void eliminar(Integer id) {
        Fatura fatura = buscarPorId(id);
        if (fatura.getValorPago().compareTo(BigDecimal.ZERO) > 0)
            throw new IllegalStateException("Não é possível eliminar uma fatura com pagamentos registados");
        faturaRepository.deleteById(id);
    }
}
