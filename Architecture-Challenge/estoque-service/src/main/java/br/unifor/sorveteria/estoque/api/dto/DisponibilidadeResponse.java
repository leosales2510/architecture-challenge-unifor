package br.unifor.sorveteria.estoque.api.dto;

import java.util.List;

import br.unifor.sorveteria.estoque.domain.Falta;

public record DisponibilidadeResponse(boolean disponivel, List<Falta> faltas) {
}
