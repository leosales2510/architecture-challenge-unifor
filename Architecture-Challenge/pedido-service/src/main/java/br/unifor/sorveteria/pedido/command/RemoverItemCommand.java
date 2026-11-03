// PADRÃO: COMMAND — papel: comando concreto.
package br.unifor.sorveteria.pedido.command;

import br.unifor.sorveteria.pedido.domain.Pedido;

public class RemoverItemCommand extends ComandoPedido {
    private final String codigoItem;
    private String descricao = "item";

    public RemoverItemCommand(String codigoItem) { this.codigoItem = codigoItem; }

    @Override protected void aplicar(Pedido receptor) {
        receptor.getItens().stream().filter(i -> i.getCodigo().equals(codigoItem)).findFirst()
                .ifPresent(i -> descricao = i.getDescricao());
        receptor.remover(codigoItem);
    }

    @Override public String getNome() { return "Remover " + descricao; }
}
