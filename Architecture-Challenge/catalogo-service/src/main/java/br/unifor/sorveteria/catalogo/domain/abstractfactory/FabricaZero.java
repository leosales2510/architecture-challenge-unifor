// PADRÃO: ABSTRACT FACTORY — papel: fábrica concreta da linha Zero Açúcar.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.CatalogoItens;
import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;

@Component
public class FabricaZero implements FabricaLinha {
    static final String SKU_BASE = "BASE-ZERO";
    static final String SKU_CASQUINHA = "CASQ-ZERO";

    private final CatalogoItens itens;

    public FabricaZero(CatalogoItens itens) { this.itens = itens; }

    @Override public Linha getLinha() { return Linha.ZERO_ACUCAR; }

    @Override public Sorvete criarSorvete(ItemCatalogo sabor) {
        return new SorveteZero(sabor, criarCasquinha(), itens.buscar(SKU_BASE).getPreco());
    }

    @Override public Casquinha criarCasquinha() { return new CasquinhaZero(itens.buscar(SKU_CASQUINHA)); }
}
