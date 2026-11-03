package br.unifor.sorveteria.catalogo.api.dto;

import java.math.BigDecimal;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.Linha;

public record LinhaResponse(Linha codigo, String nome, BigDecimal precoBase, ItemResponse casquinha,
                            List<ItemResponse> sabores) {
}
