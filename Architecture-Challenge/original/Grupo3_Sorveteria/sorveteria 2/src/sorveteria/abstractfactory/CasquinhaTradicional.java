// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Produto concreto
// ==============================================================================
package sorveteria.abstractfactory;

/** Produto concreto da linha Tradicional. */
public class CasquinhaTradicional implements Casquinha {
    @Override public String getDescricao() { return "casquinha tradicional"; }
    @Override public double getPreco() { return 2.00; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo CasquinhaTradicional.java)
