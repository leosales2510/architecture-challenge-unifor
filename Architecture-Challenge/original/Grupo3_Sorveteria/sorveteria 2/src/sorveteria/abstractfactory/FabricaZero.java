// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Fabrica concreta da linha Zero Acucar
// ==============================================================================
package sorveteria.abstractfactory;

/** Fabrica concreta: so produz itens da linha Zero Acucar. */
public class FabricaZero implements FabricaLinha {
    @Override public String getNomeDaLinha() { return "Zero Acucar"; }
    @Override public Sorvete criarSorvete(String sabor) {
        return new SorveteZero(sabor, criarCasquinha());
    }
    @Override public Casquinha criarCasquinha() { return new CasquinhaZero(); }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo FabricaZero.java)
