// ==============================================================================
// INICIO DO PADRAO: COMMAND
// Papel desta classe: Comando concreto
// ==============================================================================
package sorveteria.command;

import sorveteria.abstractfactory.Sorvete;
import sorveteria.memento.Pedido;

public class AdicionarItemCommand extends ComandoPedido {
    private final Sorvete item;

    public AdicionarItemCommand(Pedido pedido, Sorvete item) {
        super(pedido);
        this.item = item;
    }

    @Override public void executar() {
        salvarBackup();          // Memento antes de mudar o estado
        pedido.adicionar(item);  // o trabalho e do receptor, nao do comando
    }

    @Override public String getNome() { return "Adicionar item"; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND  (arquivo AdicionarItemCommand.java)
