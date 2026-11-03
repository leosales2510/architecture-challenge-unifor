package br.unifor.sorveteria.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CupomRequest(@NotBlank @Size(max = 30) String codigo) {
}
