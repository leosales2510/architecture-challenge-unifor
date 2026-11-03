package br.unifor.sorveteria.estoque.domain;

/** SKU cujo saldo não cobre o que foi pedido. */
public record Falta(String sku, int solicitado, int disponivel) {
}
