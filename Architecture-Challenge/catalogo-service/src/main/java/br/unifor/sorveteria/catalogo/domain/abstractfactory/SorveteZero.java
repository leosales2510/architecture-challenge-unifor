// PADRÃO: ABSTRACT FACTORY — papel: produto concreto da linha Zero Açúcar.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;

public class SorveteZero extends SorveteBase {
    SorveteZero(ItemCatalogo sabor, Casquinha casquinha, BigDecimal precoBase) {
        super(sabor, casquinha, precoBase);
    }

    @Override protected String prefixo() { return "Sorvete zero açúcar"; }
    @Override public Linha getLinha() { return Linha.ZERO_ACUCAR; }
}
