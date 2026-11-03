package br.unifor.sorveteria.pedido.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.ConsultaDisponibilidade;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Disponibilidade;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.FaltaRemota;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.ItemQuantidade;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Problema;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Reserva;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.ReservaRequest;
import br.unifor.sorveteria.pedido.domain.Componente;
import br.unifor.sorveteria.pedido.domain.Falta;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

@Component
public class EstoqueHttpClient implements EstoqueClient {
    private final RestClient http;

    public EstoqueHttpClient(@Qualifier("estoqueRestClient") RestClient http) { this.http = http; }

    @Override public List<Falta> verificar(List<Componente> componentes) {
        try {
            Disponibilidade d = http.post().uri("/api/estoque/disponibilidade")
                    .body(new ConsultaDisponibilidade(itens(componentes)))
                    .retrieve().body(Disponibilidade.class);
            return faltas(d.faltas());
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("estoque", e);
        }
    }

    @Override public Long reservar(Long pedidoId, List<Componente> componentes) {
        try {
            return http.post().uri("/api/estoque/reservas")
                    .body(new ReservaRequest(pedidoId, itens(componentes)))
                    .retrieve().body(Reserva.class).id();
        } catch (HttpClientErrorException e) {
            Problema p = RespostasRemotas.problema(e);
            if (e.getStatusCode() == HttpStatus.CONFLICT && p.faltas() != null) {
                throw new EstoqueInsuficienteException(faltas(p.faltas()));
            }
            throw new ServicoIndisponivelException("estoque", e);
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("estoque", e);
        }
    }

    @Override public void confirmar(Long reservaId) {
        executar(() -> http.post().uri("/api/estoque/reservas/{id}/confirmacao", reservaId).retrieve().toBodilessEntity());
    }

    @Override public void liberar(Long reservaId) {
        executar(() -> http.delete().uri("/api/estoque/reservas/{id}", reservaId).retrieve().toBodilessEntity());
    }

    private static void executar(Runnable chamada) {
        try {
            chamada.run();
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("estoque", e);
        }
    }

    private static List<ItemQuantidade> itens(List<Componente> componentes) {
        return componentes.stream().map(c -> new ItemQuantidade(c.sku(), c.quantidade())).toList();
    }

    private static List<Falta> faltas(List<FaltaRemota> remotas) {
        return remotas == null ? List.of()
                : remotas.stream().map(f -> new Falta(f.sku(), f.solicitado(), f.disponivel())).toList();
    }
}
