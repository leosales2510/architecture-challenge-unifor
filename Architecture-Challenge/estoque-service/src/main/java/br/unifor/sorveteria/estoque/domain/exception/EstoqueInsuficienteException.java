package br.unifor.sorveteria.estoque.domain.exception;

import java.util.List;

import br.unifor.sorveteria.estoque.domain.Falta;

public class EstoqueInsuficienteException extends RuntimeException {
    private final List<Falta> faltas;

    public EstoqueInsuficienteException(List<Falta> faltas) {
        super("Estoque insuficiente para: " + faltas.stream().map(Falta::sku).toList());
        this.faltas = List.copyOf(faltas);
    }

    public List<Falta> getFaltas() { return faltas; }
}
