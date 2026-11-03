// PADRÃO: DECORATOR — papel: decorator concreto.
package br.unifor.sorveteria.catalogo.domain.decorator;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;

public class Chantilly extends CoberturaDecorator {
    public Chantilly(Sorvete sorvete, ItemCatalogo dados) { super(sorvete, dados); }

    @Override protected String trechoDescricao() { return "chantilly"; }
}
