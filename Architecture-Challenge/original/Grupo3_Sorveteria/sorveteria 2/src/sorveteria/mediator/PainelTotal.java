// ==============================================================================
// INICIO DO PADRAO: MEDIATOR
// Papel desta classe: Colega concreto
// ==============================================================================
package sorveteria.mediator;

/** Mostra o total. Tambem so conversa com o mediador. */
public class PainelTotal extends Componente {
    public void atualizar(double subtotal, double desconto, double total) {
        System.out.printf("[Total] subtotal R$ %.2f | desconto %.0f%% | total R$ %.2f%n",
                subtotal, desconto * 100, total);
    }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR  (arquivo PainelTotal.java)
