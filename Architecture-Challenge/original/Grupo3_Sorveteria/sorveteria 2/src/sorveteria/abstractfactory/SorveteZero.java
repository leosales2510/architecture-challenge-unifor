// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Produto concreto
// ==============================================================================
package sorveteria.abstractfactory;

/** Produto concreto da linha Zero Acucar. */
public class SorveteZero implements Sorvete {
    private final String sabor;
    private final Casquinha casquinha;

    public SorveteZero(String sabor, Casquinha casquinha) {
        this.sabor = sabor;
        this.casquinha = casquinha;
    }

    @Override public String getDescricao() {
        return "Sorvete zero acucar de " + sabor + " na " + casquinha.getDescricao();
    }
    @Override public double getPreco() { return 11.00 + casquinha.getPreco(); }
    @Override public Casquinha getCasquinha() { return casquinha; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo SorveteZero.java)
