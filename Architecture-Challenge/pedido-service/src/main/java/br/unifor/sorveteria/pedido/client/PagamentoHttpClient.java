package br.unifor.sorveteria.pedido.client;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.PagamentoRequest;
import br.unifor.sorveteria.pedido.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

/**
 * Além de chamar a API, este cliente CLASSIFICA o resultado financeiro de cada falha:
 * "comprovadamente não cobrado" (ServicoIndisponivelException / DadosPagamentoInvalidosException)
 * ou "incerto" (PagamentoIncertoException). O checkout só libera estoque no primeiro caso.
 */
@Component
public class PagamentoHttpClient implements PagamentoClient {
    private final RestClient http;

    public PagamentoHttpClient(@Qualifier("pagamentoRestClient") RestClient http) { this.http = http; }

    @Override public Pagamento pagar(Long pedidoId, BigDecimal valor, String metodo, String numeroCartao) {
        try {
            return http.post().uri("/api/pagamentos")
                    .body(new PagamentoRequest(pedidoId, valor, metodo, numeroCartao))
                    .retrieve().body(Pagamento.class);
        } catch (HttpClientErrorException e) {           // 400/422: recusado na validação, nada cobrado
            throw new DadosPagamentoInvalidosException(RespostasRemotas.problema(e).detail());
        } catch (HttpServerErrorException e) {
            // 503 GATEWAY_INDISPONIVEL: o pagamento-service afirma que não houve aprovação nem registro
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE
                    && "GATEWAY_INDISPONIVEL".equals(RespostasRemotas.problema(e).codigo())) {
                throw new ServicoIndisponivelException("pagamentos", e);
            }
            throw new PagamentoIncertoException("Erro inesperado no serviço de pagamentos", e);
        } catch (ResourceAccessException e) {
            if (causaRaiz(e) instanceof ConnectException) {   // conexão recusada: a requisição nem chegou
                throw new ServicoIndisponivelException("pagamentos", e);
            }
            throw new PagamentoIncertoException("Resposta do serviço de pagamentos não recebida", e); // ex.: timeout de leitura
        } catch (RestClientException e) {                 // resposta chegou, mas ilegível
            throw new PagamentoIncertoException("Resposta do serviço de pagamentos ilegível", e);
        }
    }

    @Override public Optional<Pagamento> consultarAprovado(Long pedidoId) {
        try {
            Pagamento[] tentativas = http.get().uri("/api/pagamentos?pedidoId={id}", pedidoId).retrieve().body(Pagamento[].class);
            return tentativas == null ? Optional.empty() : Arrays.stream(tentativas).filter(Pagamento::aprovado).findFirst();
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("pagamentos", e);
        }
    }

    private static Throwable causaRaiz(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t;
    }
}
