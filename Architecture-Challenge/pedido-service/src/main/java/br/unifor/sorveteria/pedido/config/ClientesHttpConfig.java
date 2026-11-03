package br.unifor.sorveteria.pedido.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Um RestClient por serviço remoto, com URL configurável (variáveis de ambiente
 * CATALOGO_URL, ESTOQUE_URL, PAGAMENTO_URL) e timeouts curtos: o totem não pode
 * ficar travado esperando um serviço que caiu.
 */
@Configuration
class ClientesHttpConfig {

    @Bean
    RestClient catalogoRestClient(RestClient.Builder builder, @Value("${servicos.catalogo.url}") String url) {
        return criar(builder, url);
    }

    @Bean
    RestClient estoqueRestClient(RestClient.Builder builder, @Value("${servicos.estoque.url}") String url) {
        return criar(builder, url);
    }

    @Bean
    RestClient pagamentoRestClient(RestClient.Builder builder, @Value("${servicos.pagamento.url}") String url) {
        return criar(builder, url);
    }

    private static RestClient criar(RestClient.Builder builder, String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone().baseUrl(url).requestFactory(fabrica).build();
    }
}
