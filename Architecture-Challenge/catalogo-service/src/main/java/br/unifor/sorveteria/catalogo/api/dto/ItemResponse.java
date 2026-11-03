package br.unifor.sorveteria.catalogo.api.dto;

import java.math.BigDecimal;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;

public record ItemResponse(String sku, String nome, BigDecimal preco) {
    public static ItemResponse de(ItemCatalogo item) {
        return new ItemResponse(item.getSku(), item.getNome(), item.getPreco());
    }
}
