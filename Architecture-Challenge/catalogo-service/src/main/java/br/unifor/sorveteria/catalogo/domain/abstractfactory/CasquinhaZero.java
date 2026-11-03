// PADRÃO: ABSTRACT FACTORY — papel: produto concreto da linha Zero Açúcar.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;

public class CasquinhaZero implements Casquinha {
    private final ItemCatalogo dados;

    CasquinhaZero(ItemCatalogo dados) { this.dados = dados; }

    @Override public String getSku() { return dados.getSku(); }
    @Override public String getDescricao() { return dados.getNome(); }
    @Override public BigDecimal getPreco() { return dados.getPreco(); }
}
