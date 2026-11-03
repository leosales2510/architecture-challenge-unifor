// PADRÃO: ABSTRACT FACTORY — papel: fábrica abstrata, declara a família de produtos.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;

/**
 * Cria a família de itens de uma linha (sorvete + casquinha). São dois produtos
 * relacionados: um sorvete Zero nunca sai numa casquinha Tradicional. Com um
 * produto só, o padrão seria apenas Factory Method.
 */
public interface FabricaLinha {
    Linha getLinha();

    Sorvete criarSorvete(ItemCatalogo sabor);

    Casquinha criarCasquinha();
}
