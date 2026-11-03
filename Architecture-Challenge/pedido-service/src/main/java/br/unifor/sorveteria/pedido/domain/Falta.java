package br.unifor.sorveteria.pedido.domain;

/** SKU sem saldo suficiente, como informado pelo estoque-service. */
public record Falta(String sku, int solicitado, int disponivel) {
}
