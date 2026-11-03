// ==============================================================================
// INICIO DO PADRAO: COMMAND
// Papel desta classe: Caretaker do historico: pilha de comandos
// ==============================================================================
package sorveteria.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * CARETAKER do historico: guarda os comandos executados em uma pilha.
 * Guarda, mas nunca olha dentro do memento que o comando carrega.
 */
public class HistoricoComandos {
    private final Deque<Comando> pilha = new ArrayDeque<>();

    public void empilhar(Comando c) { pilha.push(c); }

    public Comando desempilhar() { return pilha.isEmpty() ? null : pilha.pop(); }

    public boolean estaVazio() { return pilha.isEmpty(); }

    public int tamanho() { return pilha.size(); }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND  (arquivo HistoricoComandos.java)
