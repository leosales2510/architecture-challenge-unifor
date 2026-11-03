// ==============================================================================
// INICIO DO PADRAO: MEDIATOR
// Papel desta classe: Colega abstrato
// ==============================================================================
package sorveteria.mediator;

/** Colega do Mediator: conhece o mediador, nao conhece os outros componentes. */
public abstract class Componente {
    protected Mediator mediator;

    public void setMediator(Mediator mediator) { this.mediator = mediator; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR  (arquivo Componente.java)
