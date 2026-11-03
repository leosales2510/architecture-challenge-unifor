package br.unifor.sorveteria.catalogo.api.dto;

import java.util.List;

public record CatalogoResponse(List<LinhaResponse> linhas, List<ItemResponse> coberturas, int maxCoberturas) {
}
