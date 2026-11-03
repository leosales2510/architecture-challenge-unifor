// PADRÃO: ADAPTER — papel: ADAPTER (traduz GatewayPagamento -> LegacyCardProcessor).
package br.unifor.sorveteria.pagamento.domain.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.unifor.sorveteria.pagamento.domain.ResultadoGateway;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor.LegacyAuthResponse;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor.LegacyTimeoutException;
import br.unifor.sorveteria.pagamento.domain.exception.GatewayIndisponivelException;

/**
 * Converte reais (BigDecimal) em centavos, fixa a moeda, traduz os códigos de
 * resposta para mensagens em português e transforma o timeout do SDK numa
 * exceção do nosso domínio. Nenhuma outra classe conhece o LegacyCardProcessor.
 */
public class CartaoGatewayAdapter implements GatewayPagamento {
    private final LegacyCardProcessor adaptee;

    public CartaoGatewayAdapter(LegacyCardProcessor adaptee) { this.adaptee = adaptee; }

    @Override public ResultadoGateway cobrar(SolicitacaoPagamento s) {
        LegacyAuthResponse resposta;
        try {
            resposta = adaptee.authorize(s.numeroCartao(), emCentavos(s.valor()), "BRL");
        } catch (LegacyTimeoutException e) {
            throw new GatewayIndisponivelException("Operadora de cartão indisponível", e);
        }
        return "00".equals(resposta.responseCode())
                ? ResultadoGateway.aprovado(resposta.authCode())
                : ResultadoGateway.recusado(traduzir(resposta.responseCode()));
    }

    static long emCentavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    static String traduzir(String codigo) {
        return switch (codigo) {
            case "51" -> "Saldo insuficiente";
            case "05" -> "Transação não autorizada pelo emissor";
            case "61" -> "Valor acima do limite do cartão";
            case "91" -> "Moeda não suportada";
            default -> "Recusado pela operadora (código " + codigo + ")";
        };
    }
}
