package br.unifor.sorveteria.pedido.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import br.unifor.sorveteria.pedido.domain.ItemPedido;

/** O cliente informa O QUE quer; o preço nunca vem do front-end, é sempre calculado pelo catálogo. */
public record NovoItemRequest(
        @NotBlank @Pattern(regexp = "TRADICIONAL|ZERO_ACUCAR", message = "deve ser TRADICIONAL ou ZERO_ACUCAR") String linha,
        @NotBlank String saborSku,
        @Size(max = 10) List<@NotBlank String> coberturas,
        @Min(1) @Max(ItemPedido.MAX_QUANTIDADE) int quantidade) {

    public List<String> coberturasOuVazio() { return coberturas == null ? List.of() : coberturas; }
}
