package br.unifor.sorveteria.pagamento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.unifor.sorveteria.pagamento.domain.adapter.CartaoGatewayAdapter;
import br.unifor.sorveteria.pagamento.domain.adapter.GatewayPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.PixGatewaySimulado;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor;
import br.unifor.sorveteria.pagamento.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pagamento.domain.exception.GatewayIndisponivelException;
import br.unifor.sorveteria.pagamento.domain.factorymethod.ProcessadorCartao;
import br.unifor.sorveteria.pagamento.domain.factorymethod.ProcessadorPagamento;
import br.unifor.sorveteria.pagamento.domain.factorymethod.ProcessadorPix;

class AdapterEFactoryMethodTest {
    private final LegacyCardProcessor sdk = new LegacyCardProcessor();

    private static SolicitacaoPagamento cartao(String numero, String valor) {
        return new SolicitacaoPagamento(1L, new BigDecimal(valor), MetodoPagamento.CARTAO, numero);
    }

    @Test
    void adapterTraduzAprovacaoDoSdkLegado() {
        ResultadoGateway r = new CartaoGatewayAdapter(sdk).cobrar(cartao("4111111111111111", "17.50"));
        assertThat(r.aprovado()).isTrue();
        assertThat(r.codigoAutorizacao()).hasSize(6);
    }

    @Test
    void adapterTraduzCodigosDeRecusaParaPortugues() {
        GatewayPagamento gateway = new CartaoGatewayAdapter(sdk);
        assertThat(gateway.cobrar(cartao("4000000000000002", "10.00")).motivo()).isEqualTo("Saldo insuficiente");
        assertThat(gateway.cobrar(cartao("4000000000000005", "10.00")).motivo()).contains("não autorizada");
        assertThat(gateway.cobrar(cartao("4111111111111111", "500.01")).motivo()).contains("limite");
    }

    @Test
    void adapterConverteReaisEmCentavosSemPerderPrecisao() {
        // Exatamente R$ 500,00 = 50.000 centavos: ainda dentro do limite (só acima disso é recusado).
        assertThat(new CartaoGatewayAdapter(sdk).cobrar(cartao("4111111111111111", "500.00")).aprovado()).isTrue();
    }

    @Test
    void adapterTransformaTimeoutDoSdkEmExcecaoDoDominio() {
        assertThatThrownBy(() -> new CartaoGatewayAdapter(sdk).cobrar(cartao("4000000000009999", "10.00")))
                .isInstanceOf(GatewayIndisponivelException.class);
    }

    @Test
    void factoryMethodDeCadaProcessadorCriaOGatewayCerto() {
        ProcessadorPagamento cartao = new ProcessadorCartao(sdk);
        ProcessadorPagamento pix = new ProcessadorPix();

        assertThat(cartao.processar(cartao("4111111111111111", "10.00")).aprovado()).isTrue();
        ResultadoGateway pixResultado = pix.processar(new SolicitacaoPagamento(1L, new BigDecimal("10.00"), MetodoPagamento.PIX, null));
        assertThat(pixResultado.codigoAutorizacao()).startsWith("PIX-");
    }

    @Test
    void processadorValidaOsDadosDoSeuMeio() {
        assertThatThrownBy(() -> new ProcessadorCartao(sdk).processar(cartao("123", "10.00")))
                .isInstanceOf(DadosPagamentoInvalidosException.class);
        assertThatThrownBy(() -> new ProcessadorPix().processar(cartao("4111111111111111", "10.00")))
                .isInstanceOf(DadosPagamentoInvalidosException.class);
    }

    @Test
    void pixRecusaAcimaDoLimite() {
        ResultadoGateway r = new PixGatewaySimulado().cobrar(new SolicitacaoPagamento(1L, new BigDecimal("1000.01"), MetodoPagamento.PIX, null));
        assertThat(r.aprovado()).isFalse();
    }
}
