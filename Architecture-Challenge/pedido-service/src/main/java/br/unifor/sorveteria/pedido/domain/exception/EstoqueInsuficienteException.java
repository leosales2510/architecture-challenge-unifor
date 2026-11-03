package br.unifor.sorveteria.pedido.domain.exception;

import java.util.List;

import br.unifor.sorveteria.pedido.domain.Falta;

public class EstoqueInsuficienteException extends RuntimeException {
    private final List<Falta> faltas;

    public EstoqueInsuficienteException(List<Falta> faltas) {
        super("Estoque insuficiente para: " + String.join(", ", faltas.stream().map(Falta::sku).toList()));
        this.faltas = List.copyOf(faltas);
    }

    public List<Falta> getFaltas() { return faltas; }
}
