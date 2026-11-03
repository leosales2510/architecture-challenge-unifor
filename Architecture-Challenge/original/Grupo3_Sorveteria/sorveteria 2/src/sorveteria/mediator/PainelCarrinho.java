// ==============================================================================
// INICIO DO PADRAO: MEDIATOR
// Papel desta classe: Colega concreto
// ==============================================================================
package sorveteria.mediator;

import java.util.List;

/** Mostra os itens do pedido. Nao conhece o cardapio nem o painel de total. */
public class PainelCarrinho extends Componente {
    public void clicarDesfazer() {
        System.out.println("[Carrinho] cliente clicou em DESFAZER");
        mediator.notificar(this, "desfazer");
    }

    public void clicarRemover(int posicao) {
        System.out.println("[Carrinho] cliente removeu o item " + posicao);
        mediator.notificar(this, "remover:" + posicao);
    }

    public void clicarFinalizar() {
        System.out.println("[Carrinho] cliente clicou em FINALIZAR");
        mediator.notificar(this, "finalizar");
    }

    public void atualizar(List<String> descricoes) {
        System.out.println("[Carrinho] itens:");
        if (descricoes.isEmpty()) System.out.println("   (vazio)");
        for (int i = 0; i < descricoes.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + descricoes.get(i));
        }
    }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR  (arquivo PainelCarrinho.java)
