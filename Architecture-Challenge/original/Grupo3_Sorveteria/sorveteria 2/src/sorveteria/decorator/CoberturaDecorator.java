// ==============================================================================
// INICIO DO PADRAO: DECORATOR
// Papel desta classe: Decorator base: implementa Sorvete e guarda um Sorvete
// ==============================================================================
package sorveteria.decorator;

import sorveteria.abstractfactory.Casquinha;
import sorveteria.abstractfactory.Sorvete;

/**
 * DECORATOR (base abstrata).
 * Implementa a MESMA interface do objeto que embrulha (Sorvete)
 * e guarda uma referencia a ele (agregacao). E isso que permite
 * empilhar um decorator dentro do outro.
 */
public abstract class CoberturaDecorator implements Sorvete {
    protected final Sorvete sorvete;

    protected CoberturaDecorator(Sorvete sorvete) {
        this.sorvete = sorvete;
    }

    @Override public Casquinha getCasquinha() { return sorvete.getCasquinha(); }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: DECORATOR  (arquivo CoberturaDecorator.java)
