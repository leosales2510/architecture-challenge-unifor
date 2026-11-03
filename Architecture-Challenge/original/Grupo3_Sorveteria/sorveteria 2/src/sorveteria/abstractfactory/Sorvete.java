// ==============================================================================
// INICIO DO PADRAO: ABSTRACT FACTORY + DECORATOR
// Papel desta classe: Produto abstrato da fabrica E componente que o Decorator embrulha
// ==============================================================================
package sorveteria.abstractfactory;

/**
 * Produto abstrato do Abstract Factory E componente do Decorator.
 * E este ponto que liga os dois padroes: a fabrica cria o Sorvete
 * e os decorators de cobertura embrulham esse mesmo Sorvete.
 */
public interface Sorvete {
    String getDescricao();
    double getPreco();
    Casquinha getCasquinha();
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: ABSTRACT FACTORY + DECORATOR  (arquivo Sorvete.java)
