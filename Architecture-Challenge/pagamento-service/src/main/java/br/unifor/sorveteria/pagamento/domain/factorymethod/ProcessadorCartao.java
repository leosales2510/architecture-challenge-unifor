// PADRÃO: FACTORY METHOD — papel: CONCRETE CREATOR (cria o Adapter do cartão).
package br.unifor.sorveteria.pagamento.domain.factorymethod;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.CartaoGatewayAdapter;
import br.unifor.sorveteria.pagamento.domain.adapter.GatewayPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor;
import br.unifor.sorveteria.pagamento.domain.exception.DadosPagamentoInvalidosException;

@Component
public class ProcessadorCartao extends ProcessadorPagamento {
    private final LegacyCardProcessor sdkDaOperadora;

    public ProcessadorCartao(LegacyCardProcessor sdkDaOperadora) { this.sdkDaOperadora = sdkDaOperadora; }

    @Override public MetodoPagamento metodo() { return MetodoPagamento.CARTAO; }

    @Override protected GatewayPagamento criarGateway() { return new CartaoGatewayAdapter(sdkDaOperadora); }

    @Override protected void validar(SolicitacaoPagamento s) {
        String numero = s.numeroCartao();
        if (numero == null || !numero.matches("\\d{13,19}")) {
            throw new DadosPagamentoInvalidosException("Número do cartão deve ter de 13 a 19 dígitos");
        }
    }
}
