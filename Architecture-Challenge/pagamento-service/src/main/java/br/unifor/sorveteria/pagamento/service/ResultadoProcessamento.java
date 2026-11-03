package br.unifor.sorveteria.pagamento.service;

import br.unifor.sorveteria.pagamento.domain.Pagamento;

/** @param novo false quando a chamada repetiu um pedido já aprovado (resposta idempotente) */
public record ResultadoProcessamento(Pagamento pagamento, boolean novo) {
}
