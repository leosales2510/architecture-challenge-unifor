package br.unifor.sorveteria.pedido.dto;

import java.util.List;

import br.unifor.sorveteria.pedido.checkout.ResultadoCheckout;
import br.unifor.sorveteria.pedido.domain.Falta;

/** @param trilha passos coordenados pelo Mediator, na ordem em que aconteceram */
public record CheckoutResponse(ResultadoCheckout resultado, String mensagem, List<String> trilha, List<Falta> faltas,
                               PedidoResponse pedido) {
}
