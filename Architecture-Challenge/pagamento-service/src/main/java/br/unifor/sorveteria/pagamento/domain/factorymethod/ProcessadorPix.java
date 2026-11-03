// PADRÃO: FACTORY METHOD — papel: CONCRETE CREATOR (cria o gateway Pix).
package br.unifor.sorveteria.pagamento.domain.factorymethod;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.GatewayPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.PixGatewaySimulado;
import br.unifor.sorveteria.pagamento.domain.exception.DadosPagamentoInvalidosException;

@Component
public class ProcessadorPix extends ProcessadorPagamento {

    @Override public MetodoPagamento metodo() { return MetodoPagamento.PIX; }

    @Override protected GatewayPagamento criarGateway() { return new PixGatewaySimulado(); }

    @Override protected void validar(SolicitacaoPagamento s) {
        if (s.numeroCartao() != null) {
            throw new DadosPagamentoInvalidosException("Pagamento Pix não aceita número de cartão");
        }
    }
}
