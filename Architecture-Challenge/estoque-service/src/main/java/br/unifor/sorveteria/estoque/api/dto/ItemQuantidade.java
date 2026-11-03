package br.unifor.sorveteria.estoque.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ItemQuantidade(@NotBlank String sku, @Min(value = 1, message = "deve ser no mínimo 1") int quantidade) {
}
