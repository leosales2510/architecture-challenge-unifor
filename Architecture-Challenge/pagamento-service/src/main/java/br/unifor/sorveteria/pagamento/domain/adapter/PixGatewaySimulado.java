package br.unifor.sorveteria.pagamento.domain.adapter;

import java.math.BigDecimal;
import java.util.UUID;

import br.unifor.sorveteria.pagamento.domain.ResultadoGateway;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;

/**
 * Gateway Pix simulado. Já "fala" a nossa interface, então não precisa de
 * adapter — implementa GatewayPagamento diretamente. Recusa acima do limite noturno.
 */
public class PixGatewaySimulado implements GatewayPagamento {
    static final BigDecimal LIMITE = new BigDecimal("1000.00");

    @Override public ResultadoGateway cobrar(SolicitacaoPagamento s) {
        if (s.valor().compareTo(LIMITE) > 0) return ResultadoGateway.recusado("Valor acima do limite Pix");
        return ResultadoGateway.aprovado("PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
