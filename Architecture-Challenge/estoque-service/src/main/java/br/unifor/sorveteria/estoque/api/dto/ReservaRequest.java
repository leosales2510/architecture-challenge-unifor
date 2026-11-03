package br.unifor.sorveteria.estoque.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReservaRequest(@NotNull Long pedidoId, @NotEmpty @Valid List<ItemQuantidade> itens) {
}
