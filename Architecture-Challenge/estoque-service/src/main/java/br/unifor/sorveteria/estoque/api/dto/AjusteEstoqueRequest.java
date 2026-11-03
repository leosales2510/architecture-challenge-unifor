package br.unifor.sorveteria.estoque.api.dto;

import jakarta.validation.constraints.Min;

public record AjusteEstoqueRequest(@Min(value = 0, message = "não pode ser negativa") int quantidade) {
}
