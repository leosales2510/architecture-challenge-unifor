// ==============================================================================
// INICIO DO PADRAO: DECORATOR
// Papel desta classe: Decorator concreto
// ==============================================================================
package sorveteria.decorator;

import sorveteria.abstractfactory.Sorvete;

/** Decorator concreto: soma descricao e preco da calda. */
public class CaldaChocolate extends CoberturaDecorator {
    public CaldaChocolate(Sorvete sorvete) { super(sorvete); }

    @Override public String getDescricao() { return sorvete.getDescricao() + " + calda de chocolate"; }
    @Override public double getPreco() { return sorvete.getPreco() + 2.50; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: DECORATOR  (arquivo CaldaChocolate.java)
