package br.unifor.sorveteria.catalogo.api.dto;

/** Item de estoque consumido por UMA unidade do produto montado. */
public record ComponenteResponse(String sku, int quantidade) {
}
