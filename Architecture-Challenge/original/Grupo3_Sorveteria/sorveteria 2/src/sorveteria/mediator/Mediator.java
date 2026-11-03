// ==============================================================================
// INICIO DO PADRAO: MEDIATOR
// Papel desta classe: Interface do mediador
// ==============================================================================
package sorveteria.mediator;

/** MEDIATOR: os componentes avisam o mediador em vez de falarem entre si. */
public interface Mediator {
    void notificar(Componente remetente, String evento);
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR  (arquivo Mediator.java)
