// ==============================================================================
// INICIO DO PADRAO: COMMAND
// Papel desta classe: Comando concreto
// ==============================================================================
package sorveteria.command;

import sorveteria.memento.Pedido;

public class AplicarCupomCommand extends ComandoPedido {
    private final String codigo;

    public AplicarCupomCommand(Pedido pedido, String codigo) {
        super(pedido);
        this.codigo = codigo;
    }

    @Override public void executar() {
        salvarBackup();
        pedido.aplicarCupom(codigo);
    }

    @Override public String getNome() { return "Aplicar cupom " + codigo; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND  (arquivo AplicarCupomCommand.java)
