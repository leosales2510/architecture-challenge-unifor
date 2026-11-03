// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Fabrica concreta da linha Tradicional
// ==============================================================================
package sorveteria.abstractfactory;

/** Fabrica concreta: so produz itens da linha Tradicional. */
public class FabricaTradicional implements FabricaLinha {
    @Override public String getNomeDaLinha() { return "Tradicional"; }
    @Override public Sorvete criarSorvete(String sabor) {
        return new SorveteTradicional(sabor, criarCasquinha());
    }
    @Override public Casquinha criarCasquinha() { return new CasquinhaTradicional(); }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo FabricaTradicional.java)
