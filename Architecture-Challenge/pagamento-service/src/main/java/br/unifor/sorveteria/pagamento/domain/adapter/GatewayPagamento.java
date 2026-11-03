// PADRÃO: ADAPTER — papel: TARGET (a interface que o nosso sistema espera).
package br.unifor.sorveteria.pagamento.domain.adapter;

import br.unifor.sorveteria.pagamento.domain.ResultadoGateway;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;

public interface GatewayPagamento {
    /**
     * @return aprovado ou recusado (recusa é um resultado de negócio, não uma exceção)
     * @throws br.unifor.sorveteria.pagamento.domain.exception.GatewayIndisponivelException se o provedor não responder
     */
    ResultadoGateway cobrar(SolicitacaoPagamento solicitacao);
}
