package br.unifor.sorveteria.pedido.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Montagem;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.domain.Componente;
import br.unifor.sorveteria.pedido.domain.Falta;
import br.unifor.sorveteria.pedido.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.ItemInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

/**
 * Testes de contrato da comunicação entre serviços: verificam o JSON que o
 * pedido-service ENVIA e como ele INTERPRETA cada resposta (sucesso, 4xx, 5xx, rede).
 * O servidor remoto é simulado pelo MockRestServiceServer do Spring.
 */
class ContratosHttpTest {
    private static final MediaType PROBLEMA = MediaType.APPLICATION_PROBLEM_JSON;

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://remoto");
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
    private final RestClient http = builder.build();

    @Test
    void catalogoMontagemOk() {
        servidor.expect(requestTo("http://remoto/api/catalogo/montagens")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA"]}"""))
                .andRespond(withSuccess("""
                        {"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA"],
                         "descricao":"Sorvete zero","precoUnitario":16.00,
                         "componentes":[{"sku":"SAB-ZERO-MOR","quantidade":1}],"campoNovo":"ignorado"}""",
                        MediaType.APPLICATION_JSON));

        Montagem m = new CatalogoHttpClient(http).montar("ZERO_ACUCAR", "SAB-ZERO-MOR", List.of("COB-CALDA"));

        assertThat(m.precoUnitario()).isEqualByComparingTo("16.00");
        assertThat(m.componentes()).hasSize(1);
        servidor.verify();
    }

    @Test
    void catalogo422ViraItemInvalidoComAMensagemDoCatalogo() {
        servidor.expect(requestTo("http://remoto/api/catalogo/montagens"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(PROBLEMA).body("""
                        {"codigo":"COMPOSICAO_INVALIDA","detail":"Máximo de 3 coberturas por sorvete"}"""));

        assertThatThrownBy(() -> new CatalogoHttpClient(http).montar("TRADICIONAL", "X", List.of()))
                .isInstanceOf(ItemInvalidoException.class).hasMessage("Máximo de 3 coberturas por sorvete");
    }

    @Test
    void catalogoForaDoArViraServicoIndisponivel() {
        servidor.expect(requestTo("http://remoto/api/catalogo/montagens")).andRespond(withException(new IOException("refused")));

        assertThatThrownBy(() -> new CatalogoHttpClient(http).montar("TRADICIONAL", "X", List.of()))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    void estoqueReservaOkDevolveId() {
        servidor.expect(requestTo("http://remoto/api/estoque/reservas"))
                .andExpect(content().json("""
                        {"pedidoId":5,"itens":[{"sku":"CASQ-ZERO","quantidade":2}]}"""))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("""
                        {"id":77,"pedidoId":5,"status":"PENDENTE"}"""));

        assertThat(new EstoqueHttpClient(http).reservar(5L, List.of(new Componente("CASQ-ZERO", 2)))).isEqualTo(77L);
    }

    @Test
    void estoque409ViraEstoqueInsuficienteComAsFaltas() {
        servidor.expect(requestTo("http://remoto/api/estoque/reservas"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(PROBLEMA).body("""
                        {"codigo":"ESTOQUE_INSUFICIENTE","detail":"x","faltas":[{"sku":"COB-CAST","solicitado":3,"disponivel":2}]}"""));

        assertThatThrownBy(() -> new EstoqueHttpClient(http).reservar(5L, List.of(new Componente("COB-CAST", 3))))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .satisfies(e -> assertThat(((EstoqueInsuficienteException) e).getFaltas())
                        .containsExactly(new Falta("COB-CAST", 3, 2)));
    }

    @Test
    void estoqueLiberacaoUsaDelete() {
        servidor.expect(requestTo("http://remoto/api/estoque/reservas/77")).andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        new EstoqueHttpClient(http).liberar(77L);
        servidor.verify();
    }

    @Test
    void pagamentoRecusadoNaoEhExcecao() {
        servidor.expect(requestTo("http://remoto/api/pagamentos"))
                .andExpect(content().json("""
                        {"pedidoId":5,"valor":17.50,"metodo":"CARTAO","numeroCartao":"4000000000000002"}"""))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("""
                        {"id":3,"pedidoId":5,"valor":17.50,"metodo":"CARTAO","status":"RECUSADO","motivoRecusa":"Saldo insuficiente"}"""));

        Pagamento p = new PagamentoHttpClient(http).pagar(5L, new BigDecimal("17.50"), "CARTAO", "4000000000000002");

        assertThat(p.aprovado()).isFalse();
        assertThat(p.motivoRecusa()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void timeoutDeLeituraNoPagamentoNaoEhTratadoComoNaoCobrado() {
        // a requisição chegou ao pagamento-service, mas a resposta se perdeu: a cobrança pode ter ocorrido
        servidor.expect(requestTo("http://remoto/api/pagamentos")).andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> new PagamentoHttpClient(http).pagar(5L, BigDecimal.TEN, "CARTAO", "4111111111111111"))
                .isInstanceOf(PagamentoIncertoException.class);
    }

    @Test
    void conexaoRecusadaNoPagamentoEhComprovadamenteNaoCobrado() {
        servidor.expect(requestTo("http://remoto/api/pagamentos")).andRespond(withException(new ConnectException("Connection refused")));

        assertThatThrownBy(() -> new PagamentoHttpClient(http).pagar(5L, BigDecimal.TEN, "CARTAO", "4111111111111111"))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    void erro500DoPagamentoEhIncerto() {
        servidor.expect(requestTo("http://remoto/api/pagamentos")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> new PagamentoHttpClient(http).pagar(5L, BigDecimal.TEN, "CARTAO", "4111111111111111"))
                .isInstanceOf(PagamentoIncertoException.class);
    }

    @Test
    void reconciliacaoConsultaAsTentativasDoPedido() {
        servidor.expect(requestTo("http://remoto/api/pagamentos?pedidoId=5")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":8,"pedidoId":5,"status":"RECUSADO"},{"id":7,"pedidoId":5,"status":"APROVADO","codigoAutorizacao":"111222"}]""",
                        MediaType.APPLICATION_JSON));

        assertThat(new PagamentoHttpClient(http).consultarAprovado(5L)).get().extracting(Pagamento::id).isEqualTo(7L);
    }

    @Test
    void pagamento503EPagamento422() {
        servidor.expect(requestTo("http://remoto/api/pagamentos"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).contentType(PROBLEMA).body("{\"codigo\":\"GATEWAY_INDISPONIVEL\"}"));
        servidor.expect(requestTo("http://remoto/api/pagamentos"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(PROBLEMA).body("""
                        {"codigo":"DADOS_PAGAMENTO_INVALIDOS","detail":"Número do cartão deve ter de 13 a 19 dígitos"}"""));
        PagamentoHttpClient cliente = new PagamentoHttpClient(http);

        assertThatThrownBy(() -> cliente.pagar(5L, BigDecimal.TEN, "CARTAO", "4000000000009999"))
                .isInstanceOf(ServicoIndisponivelException.class);
        assertThatThrownBy(() -> cliente.pagar(5L, BigDecimal.TEN, "CARTAO", "1"))
                .isInstanceOf(DadosPagamentoInvalidosException.class).hasMessageContaining("13 a 19");
    }
}
