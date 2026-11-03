// ==============================================================================
// INICIO DO PADRAO: COMMAND
// Papel desta classe: Comando concreto
// ==============================================================================
package sorveteria.command;

import sorveteria.abstractfactory.Sorvete;
import sorveteria.memento.Pedido;

public class RemoverItemCommand extends ComandoPedido {
    private final Sorvete item;

    public RemoverItemCommand(Pedido pedido, Sorvete item) {
        super(pedido);
        this.item = item;
    }

    @Override public void executar() {
        salvarBackup();
        pedido.remover(item);
    }

    @Override public String getNome() { return "Remover item"; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND  (arquivo RemoverItemCommand.java)
