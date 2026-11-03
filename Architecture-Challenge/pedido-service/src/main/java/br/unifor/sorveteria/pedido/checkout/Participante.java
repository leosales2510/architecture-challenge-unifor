// PADRÃO: MEDIATOR — papel: colega abstrato (equivale ao "Componente" do projeto original).
package br.unifor.sorveteria.pedido.checkout;

/** Conhece o mediador; não conhece os outros participantes. */
public abstract class Participante {
    protected CheckoutMediator mediator;

    void setMediator(CheckoutMediator mediator) { this.mediator = mediator; }

    protected void avisar(EventoCheckout evento, ContextoCheckout contexto) {
        mediator.notificar(this, evento, contexto);
    }
}
