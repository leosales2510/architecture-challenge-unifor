// PADRÃO: ABSTRACT FACTORY — papel: produto abstrato (2º produto da família).
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;

public interface Casquinha {
    String getSku();
    String getDescricao();
    BigDecimal getPreco();
}
