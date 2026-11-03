// ==============================================================================
// INICIO DO PADRAO: SIMULACAO
// Papel desta classe: Roteiro do totem: mostra os 5 padroes em acao
// ==============================================================================
package sorveteria;

import sorveteria.mediator.PainelCardapio;
import sorveteria.mediator.PainelCarrinho;
import sorveteria.mediator.PainelTotal;
import sorveteria.mediator.TotemMediator;

/**
 * Simulacao do totem de autoatendimento da sorveteria.
 * Grupo 3 - Abstract Factory, Decorator, Command, Mediator e Memento.
 */
public class Main {

    public static void main(String[] args) {
        PainelCardapio cardapio = new PainelCardapio();
        PainelCarrinho carrinho = new PainelCarrinho();
        PainelTotal total = new PainelTotal();
        TotemMediator totem = new TotemMediator(cardapio, carrinho, total);

        // [ABSTRACT FACTORY]
        etapa("1. Escolher a linha (Abstract Factory)");
        cardapio.escolherLinha("Zero");

        // [ABSTRACT FACTORY + DECORATOR]
        etapa("2. Montar sorvete com coberturas (Abstract Factory + Decorator)");
        cardapio.selecionar("morango", "calda", "granulado");

        // [COMMAND + MEMENTO + MEDIATOR]
        etapa("3. Adicionar um segundo item (Command + Memento + Mediator)");
        cardapio.selecionar("limao");

        // [COMMAND]
        etapa("4. Aplicar cupom (Command)");
        totem.aplicarCupom();

        // [MEMENTO]
        etapa("5. Desfazer o cupom (Memento)");
        carrinho.clicarDesfazer();

        etapa("6. Desfazer o segundo item (Memento)");
        carrinho.clicarDesfazer();

        // [COMMAND]
        etapa("7. Remover um item (Command)");
        carrinho.clicarRemover(1);

        etapa("8. Desfazer a remocao (Memento)");
        carrinho.clicarDesfazer();

        etapa("9. Finalizar o pedido");
        carrinho.clicarFinalizar();
    }

    private static void etapa(String titulo) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
    }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: SIMULACAO  (arquivo Main.java)
