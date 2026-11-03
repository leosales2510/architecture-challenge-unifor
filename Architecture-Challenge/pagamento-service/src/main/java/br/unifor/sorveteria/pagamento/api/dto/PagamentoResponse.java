package br.unifor.sorveteria.pagamento.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.Pagamento;
import br.unifor.sorveteria.pagamento.domain.StatusPagamento;

public record PagamentoResponse(Long id, Long pedidoId, BigDecimal valor, MetodoPagamento metodo, StatusPagamento status,
                                String codigoAutorizacao, String motivoRecusa, String finalCartao, Instant criadoEm) {
    public static PagamentoResponse de(Pagamento p) {
        return new PagamentoResponse(p.getId(), p.getPedidoId(), p.getValor(), p.getMetodo(), p.getStatus(),
                p.getCodigoAutorizacao(), p.getMotivoRecusa(), p.getFinalCartao(), p.getCriadoEm());
    }
}
