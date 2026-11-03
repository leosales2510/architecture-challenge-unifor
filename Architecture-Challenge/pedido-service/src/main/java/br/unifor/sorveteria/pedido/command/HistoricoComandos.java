// PADRÃO: COMMAND + MEMENTO — papel: CARETAKER (pilha de comandos executados).
package br.unifor.sorveteria.pedido.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Guarda os comandos executados de UM pedido. Guarda, mas nunca olha dentro do
 * memento que cada comando carrega. Limitado para não crescer sem fim.
 */
public class HistoricoComandos {
    public static final int LIMITE = 20;

    private final Deque<Comando> pilha = new ArrayDeque<>();

    public synchronized void empilhar(Comando comando) {
        pilha.push(comando);
        if (pilha.size() > LIMITE) pilha.removeLast(); // descarta o mais antigo
    }

    public synchronized Optional<Comando> desempilhar() { return Optional.ofNullable(pilha.poll()); }

    public synchronized boolean estaVazio() { return pilha.isEmpty(); }

    public synchronized int tamanho() { return pilha.size(); }

    /** Nomes do topo para a base (o primeiro é o que será desfeito). */
    public synchronized List<String> nomes() { return pilha.stream().map(Comando::getNome).toList(); }
}
