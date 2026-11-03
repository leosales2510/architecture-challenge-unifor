package br.unifor.sorveteria.catalogo.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.unifor.sorveteria.catalogo.domain.exception.RecursoNaoEncontradoException;

/** Dublê de teste da porta CatalogoItens: permite testar os padrões sem Spring nem banco. */
public class CatalogoEmMemoria implements CatalogoItens {
    private final Map<String, ItemCatalogo> itens = new HashMap<>();

    public CatalogoEmMemoria() {
        add("BASE-TRAD", "Sorvete tradicional", TipoItem.BASE, Linha.TRADICIONAL, "9.00");
        add("BASE-ZERO", "Sorvete zero açúcar", TipoItem.BASE, Linha.ZERO_ACUCAR, "11.00");
        add("CASQ-TRAD", "casquinha tradicional", TipoItem.CASQUINHA, Linha.TRADICIONAL, "2.00");
        add("CASQ-ZERO", "casquinha integral sem açúcar", TipoItem.CASQUINHA, Linha.ZERO_ACUCAR, "2.50");
        add("SAB-TRAD-CHOC", "Chocolate", TipoItem.SABOR, Linha.TRADICIONAL, "0");
        add("SAB-ZERO-MOR", "Morango", TipoItem.SABOR, Linha.ZERO_ACUCAR, "0");
        add("COB-CALDA", "Calda de chocolate", TipoItem.COBERTURA, null, "2.50");
        add("COB-GRAN", "Granulado", TipoItem.COBERTURA, null, "1.50");
        add("COB-CHANT", "Chantilly", TipoItem.COBERTURA, null, "2.00");
        add("COB-CAST", "Castanha caramelizada", TipoItem.COBERTURA, null, "3.00");
        add("COB-NOVA", "Cobertura sem decorator", TipoItem.COBERTURA, null, "1.00");
    }

    private void add(String sku, String nome, TipoItem tipo, Linha linha, String preco) {
        itens.put(sku, new ItemCatalogo(sku, nome, tipo, linha, new BigDecimal(preco)));
    }

    @Override public ItemCatalogo buscar(String sku) {
        ItemCatalogo item = itens.get(sku);
        if (item == null) throw new RecursoNaoEncontradoException("Item do catálogo não encontrado: " + sku);
        return item;
    }
}
