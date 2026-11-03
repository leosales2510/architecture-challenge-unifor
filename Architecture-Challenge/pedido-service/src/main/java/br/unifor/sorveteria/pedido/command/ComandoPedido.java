// PADRÃO: COMMAND + MEMENTO — papel: base dos comandos; guarda o memento e sabe desfazer.
package br.unifor.sorveteria.pedido.command;

import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.SnapshotPedido;

/**
 * AQUI COMMAND E MEMENTO SE ENCONTRAM (igual ao projeto original): antes de agir,
 * o comando pede um snapshot ao Pedido; no desfazer(), manda o Pedido restaurá-lo.
 * O comando guarda o snapshot, mas não consegue ler o conteúdo dele.
 */
public abstract class ComandoPedido implements Comando {
    private SnapshotPedido backup;

    /** Template: salva o backup, executa a ação concreta; se a ação falhar, não há backup (nada a desfazer). */
    @Override public final void executar(Pedido receptor) {
        SnapshotPedido antes = receptor.criarSnapshot();
        aplicar(receptor);
        this.backup = antes;
    }

    /** A ação em si. O trabalho é do receptor (Pedido), não do comando. */
    protected abstract void aplicar(Pedido receptor);

    @Override public void desfazer(Pedido receptor) {
        if (backup == null) throw new IllegalStateException("Comando " + getNome() + " não foi executado");
        receptor.restaurar(backup);
    }
}
