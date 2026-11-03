// PADRÃO: COMMAND — papel: comando concreto.
package br.unifor.sorveteria.pedido.command;

import br.unifor.sorveteria.pedido.domain.ItemPedido;
import br.unifor.sorveteria.pedido.domain.Pedido;

public class AdicionarItemCommand extends ComandoPedido {
    private final ItemPedido item;

    public AdicionarItemCommand(ItemPedido item) { this.item = item; }

    @Override protected void aplicar(Pedido receptor) { receptor.adicionar(item); }

    @Override public String getNome() { return "Adicionar " + item.getQuantidade() + "x " + item.getDescricao(); }
}
