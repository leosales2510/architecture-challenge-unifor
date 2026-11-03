// PADRÃO: ABSTRACT FACTORY — papel: fábrica concreta da linha Tradicional.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.CatalogoItens;
import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;

@Component
public class FabricaTradicional implements FabricaLinha {
    static final String SKU_BASE = "BASE-TRAD";
    static final String SKU_CASQUINHA = "CASQ-TRAD";

    private final CatalogoItens itens;

    public FabricaTradicional(CatalogoItens itens) { this.itens = itens; }

    @Override public Linha getLinha() { return Linha.TRADICIONAL; }

    @Override public Sorvete criarSorvete(ItemCatalogo sabor) {
        return new SorveteTradicional(sabor, criarCasquinha(), itens.buscar(SKU_BASE).getPreco());
    }

    @Override public Casquinha criarCasquinha() { return new CasquinhaTradicional(itens.buscar(SKU_CASQUINHA)); }
}
