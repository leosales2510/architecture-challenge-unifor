// PADRÃO: MEDIATOR (herdado do projeto do Grupo 3) — papel: interface do mediador.
package br.unifor.sorveteria.pedido.checkout;

/** Os participantes avisam o mediador em vez de falarem entre si. */
public interface CheckoutMediator {
    void finalizar(ContextoCheckout contexto);

    void notificar(Participante remetente, EventoCheckout evento, ContextoCheckout contexto);
}
