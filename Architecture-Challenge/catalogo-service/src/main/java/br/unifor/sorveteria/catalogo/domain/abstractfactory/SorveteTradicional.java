// PADRÃO: ABSTRACT FACTORY — papel: produto concreto da linha Tradicional.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;

public class SorveteTradicional extends SorveteBase {
    SorveteTradicional(ItemCatalogo sabor, Casquinha casquinha, BigDecimal precoBase) {
        super(sabor, casquinha, precoBase);
    }

    @Override protected String prefixo() { return "Sorvete tradicional"; }
    @Override public Linha getLinha() { return Linha.TRADICIONAL; }
}
