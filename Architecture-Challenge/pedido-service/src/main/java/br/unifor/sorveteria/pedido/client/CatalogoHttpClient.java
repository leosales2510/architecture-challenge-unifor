package br.unifor.sorveteria.pedido.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Montagem;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.MontagemRequest;
import br.unifor.sorveteria.pedido.domain.exception.ItemInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

@Component
public class CatalogoHttpClient implements CatalogoClient {
    private final RestClient http;

    public CatalogoHttpClient(@Qualifier("catalogoRestClient") RestClient http) { this.http = http; }

    @Override public Montagem montar(String linha, String saborSku, List<String> coberturas) {
        try {
            return http.post().uri("/api/catalogo/montagens")
                    .body(new MontagemRequest(linha, saborSku, coberturas))
                    .retrieve().body(Montagem.class);
        } catch (HttpClientErrorException e) {          // 4xx: o catálogo recusou os dados
            throw new ItemInvalidoException(RespostasRemotas.problema(e).detail());
        } catch (RestClientException e) {                // 5xx, conexão recusada, timeout
            throw new ServicoIndisponivelException("catálogo", e);
        }
    }
}
