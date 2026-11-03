package br.unifor.sorveteria.estoque.api.dto;

import java.time.Instant;
import java.util.List;

import br.unifor.sorveteria.estoque.domain.Reserva;
import br.unifor.sorveteria.estoque.domain.StatusReserva;

public record ReservaResponse(Long id, Long pedidoId, StatusReserva status, Instant criadaEm, List<ItemQuantidade> itens) {
    public static ReservaResponse de(Reserva r) {
        return new ReservaResponse(r.getId(), r.getPedidoId(), r.getStatus(), r.getCriadaEm(),
                r.getItens().stream().map(i -> new ItemQuantidade(i.getSku(), i.getQuantidade())).toList());
    }
}
