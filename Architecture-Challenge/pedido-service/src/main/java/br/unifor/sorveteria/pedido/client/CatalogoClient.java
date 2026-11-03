package br.unifor.sorveteria.pedido.client;

import java.util.List;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Montagem;

/** Porta para o catalogo-service (DIP: a fachada depende desta interface, não do HTTP). */
public interface CatalogoClient {
    /**
     * @throws br.unifor.sorveteria.pedido.domain.exception.ItemInvalidoException se o catálogo recusar a montagem
     * @throws br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException se o catálogo não responder
     */
    Montagem montar(String linha, String saborSku, List<String> coberturas);
}
