// PADRÃO: ABSTRACT FACTORY — base comum dos produtos concretos "Sorvete" (evita duplicação).
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;

abstract class SorveteBase implements Sorvete {
    private final ItemCatalogo sabor;
    private final Casquinha casquinha;
    private final BigDecimal precoBase;

    SorveteBase(ItemCatalogo sabor, Casquinha casquinha, BigDecimal precoBase) {
        this.sabor = sabor;
        this.casquinha = casquinha;
        this.precoBase = precoBase;
    }

    /** Ex.: "Sorvete tradicional", "Sorvete zero açúcar". */
    protected abstract String prefixo();

    @Override public String getDescricao() {
        return prefixo() + " de " + sabor.getNome().toLowerCase() + " na " + casquinha.getDescricao();
    }

    @Override public BigDecimal getPreco() { return precoBase.add(casquinha.getPreco()); }

    @Override public Casquinha getCasquinha() { return casquinha; }

    @Override public List<String> getComponentes() { return List.of(sabor.getSku(), casquinha.getSku()); }
}
