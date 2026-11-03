// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY
// Papel desta classe: Fabrica abstrata: declara a familia de produtos
// ==============================================================================
package sorveteria.abstractfactory;

/**
 * ABSTRACT FACTORY.
 * Cria a familia de itens de uma linha (sorvete + casquinha).
 * Sao dois produtos: com um so, o padrao seria apenas Factory Method.
 */
public interface FabricaLinha {
    String getNomeDaLinha();
    Sorvete criarSorvete(String sabor);
    Casquinha criarCasquinha();
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY  (arquivo FabricaLinha.java)
