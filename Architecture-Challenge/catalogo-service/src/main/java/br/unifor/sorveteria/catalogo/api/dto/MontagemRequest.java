package br.unifor.sorveteria.catalogo.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.unifor.sorveteria.catalogo.domain.Linha;

/** Pedido de montagem de UM sorvete. A regra do máximo de coberturas fica no Builder. */
public record MontagemRequest(
        @NotNull(message = "é obrigatória") Linha linha,
        @NotBlank(message = "é obrigatório") String saborSku,
        @Size(max = 10, message = "lista grande demais") List<@NotBlank String> coberturas) {

    public List<String> coberturasOuVazio() {
        return coberturas == null ? List.of() : coberturas;
    }
}
