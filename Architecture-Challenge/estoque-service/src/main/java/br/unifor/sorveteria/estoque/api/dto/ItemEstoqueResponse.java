package br.unifor.sorveteria.estoque.api.dto;

import br.unifor.sorveteria.estoque.domain.ItemEstoque;

public record ItemEstoqueResponse(String sku, String descricao, int quantidade) {
    public static ItemEstoqueResponse de(ItemEstoque i) {
        return new ItemEstoqueResponse(i.getSku(), i.getDescricao(), i.getQuantidade());
    }
}
