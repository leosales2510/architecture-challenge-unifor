package br.unifor.sorveteria.estoque.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ConsultaDisponibilidadeRequest(@NotEmpty @Valid List<ItemQuantidade> itens) {
}
