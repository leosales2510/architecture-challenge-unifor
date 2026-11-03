// PADRÃO: DECORATOR — papel: decorator concreto.
package br.unifor.sorveteria.catalogo.domain.decorator;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;

public class CaldaChocolate extends CoberturaDecorator {
    public CaldaChocolate(Sorvete sorvete, ItemCatalogo dados) { super(sorvete, dados); }

    @Override protected String trechoDescricao() { return "calda de chocolate"; }
}
