// ==============================================================================
// INICIO DO PADRAO: DECORATOR
// Papel desta classe: Decorator concreto
// ==============================================================================
package sorveteria.decorator;

import sorveteria.abstractfactory.Sorvete;

/** Decorator concreto: soma descricao e preco do granulado. */
public class Granulado extends CoberturaDecorator {
    public Granulado(Sorvete sorvete) { super(sorvete); }

    @Override public String getDescricao() { return sorvete.getDescricao() + " + granulado"; }
    @Override public double getPreco() { return sorvete.getPreco() + 1.50; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: DECORATOR  (arquivo Granulado.java)
