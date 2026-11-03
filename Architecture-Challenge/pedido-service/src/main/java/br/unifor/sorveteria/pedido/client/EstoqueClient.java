package br.unifor.sorveteria.pedido.client;

import java.util.List;

import br.unifor.sorveteria.pedido.domain.Componente;
import br.unifor.sorveteria.pedido.domain.Falta;

/** Porta para o estoque-service. */
public interface EstoqueClient {
    List<Falta> verificar(List<Componente> componentes);

    /** @throws br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException se faltar algum SKU */
    Long reservar(Long pedidoId, List<Componente> componentes);

    void confirmar(Long reservaId);

    void liberar(Long reservaId);
}
