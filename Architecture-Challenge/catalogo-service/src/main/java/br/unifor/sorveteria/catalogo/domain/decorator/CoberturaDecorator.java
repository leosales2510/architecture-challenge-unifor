// PADRÃO: DECORATOR (herdado do projeto do Grupo 3)
// Papel: decorator base — implementa Sorvete e guarda um Sorvete, por isso pode ser empilhado.
package br.unifor.sorveteria.catalogo.domain.decorator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Casquinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;

/**
 * Cada cobertura acrescenta descrição, preço e um SKU de estoque ao sorvete que
 * embrulha. O preço vem do cadastro do catálogo (não fica fixo no código como no
 * projeto original), mas o texto e a forma de compor continuam na subclasse.
 */
public abstract class CoberturaDecorator implements Sorvete {
    protected final Sorvete sorvete;
    private final ItemCatalogo cobertura;

    protected CoberturaDecorator(Sorvete sorvete, ItemCatalogo cobertura) {
        this.sorvete = sorvete;
        this.cobertura = cobertura;
    }

    /** Trecho acrescentado à descrição do sorvete embrulhado, ex.: "calda de chocolate". */
    protected abstract String trechoDescricao();

    @Override public String getDescricao() { return sorvete.getDescricao() + " + " + trechoDescricao(); }

    @Override public BigDecimal getPreco() { return sorvete.getPreco().add(cobertura.getPreco()); }

    @Override public Casquinha getCasquinha() { return sorvete.getCasquinha(); }

    @Override public Linha getLinha() { return sorvete.getLinha(); }

    @Override public List<String> getComponentes() {
        List<String> componentes = new ArrayList<>(sorvete.getComponentes());
        componentes.add(cobertura.getSku());
        return List.copyOf(componentes);
    }
}
