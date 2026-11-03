package br.unifor.sorveteria.catalogo.api.dto;

import java.math.BigDecimal;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.Linha;

/** Contrato consumido pelo pedido-service: o preço oficial do produto é sempre calculado aqui. */
public record MontagemResponse(Linha linha, String saborSku, List<String> coberturas, String descricao,
                               BigDecimal precoUnitario, List<ComponenteResponse> componentes) {
}
