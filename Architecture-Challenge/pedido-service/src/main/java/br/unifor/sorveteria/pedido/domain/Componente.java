package br.unifor.sorveteria.pedido.domain;

/** SKU de estoque consumido por uma unidade de um item (vem do catálogo). */
public record Componente(String sku, int quantidade) {
}
