// ==============================================================================
// INICIO DO PADRAO: COMMAND + MEMENTO
// Papel desta classe: Base dos comandos: guarda o memento e sabe desfazer
// ==============================================================================
package sorveteria.command;

import sorveteria.memento.Pedido;
import sorveteria.memento.SnapshotPedido;

/**
 * Base dos comandos que mexem no Pedido.
 * AQUI COMMAND E MEMENTO SE ENCONTRAM: antes de agir, o comando pede
 * um snapshot ao Pedido; no desfazer(), manda o Pedido restaurar esse snapshot.
 */
public abstract class ComandoPedido implements Comando {
    protected final Pedido pedido;   // receptor (quem faz o trabalho de verdade)
    protected SnapshotPedido backup; // memento guardado pelo comando

    protected ComandoPedido(Pedido pedido) { this.pedido = pedido; }

    protected void salvarBackup() { this.backup = pedido.criarSnapshot(); }

    @Override public void desfazer() {
        if (backup != null) pedido.restaurar(backup);
    }

    @Override public abstract void executar();
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND + MEMENTO  (arquivo ComandoPedido.java)
