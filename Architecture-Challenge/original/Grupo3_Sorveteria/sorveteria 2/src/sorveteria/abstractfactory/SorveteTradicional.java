// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Produto concreto
// ==============================================================================
package sorveteria.abstractfactory;

/** Produto concreto da linha Tradicional. */
public class SorveteTradicional implements Sorvete {
    private final String sabor;
    private final Casquinha casquinha;

    public SorveteTradicional(String sabor, Casquinha casquinha) {
        this.sabor = sabor;
        this.casquinha = casquinha;
    }

    @Override public String getDescricao() {
        return "Sorvete tradicional de " + sabor + " na " + casquinha.getDescricao();
    }
    @Override public double getPreco() { return 9.00 + casquinha.getPreco(); }
    @Override public Casquinha getCasquinha() { return casquinha; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo SorveteTradicional.java)
