// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Produto concreto
// ==============================================================================
package sorveteria.abstractfactory;

/** Produto concreto da linha Zero Acucar. */
public class CasquinhaZero implements Casquinha {
    @Override public String getDescricao() { return "casquinha integral sem acucar"; }
    @Override public double getPreco() { return 2.50; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo CasquinhaZero.java)
