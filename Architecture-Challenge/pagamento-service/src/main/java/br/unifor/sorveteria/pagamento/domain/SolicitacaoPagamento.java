package br.unifor.sorveteria.pagamento.domain;

import java.math.BigDecimal;

/** Dados de uma cobrança já validados sintaticamente pela API. */
public record SolicitacaoPagamento(Long pedidoId, BigDecimal valor, MetodoPagamento metodo, String numeroCartao) {
}
