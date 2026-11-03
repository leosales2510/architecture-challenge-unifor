// PADRÃO: BUILDER (novo no Architecture Challenge)
// Papel: monta passo a passo um sorvete personalizado e só entrega o produto se ele for válido.
package br.unifor.sorveteria.catalogo.domain.builder;

import java.util.ArrayList;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.TipoItem;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricaLinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;
import br.unifor.sorveteria.catalogo.domain.decorator.CoberturasDisponiveis;
import br.unifor.sorveteria.catalogo.domain.exception.ComposicaoInvalidaException;

/**
 * No projeto original a montagem estava espalhada dentro do TotemMediator
 * (fábrica + switch de coberturas). O Builder separa a CONSTRUÇÃO do produto
 * da sua REPRESENTAÇÃO: o cliente informa as partes em qualquer ordem e o
 * construir() aplica as regras (linha obrigatória, sabor da mesma linha,
 * no máximo 3 coberturas, sem repetição) antes de usar a Abstract Factory e
 * empilhar os Decorators.
 *
 * <p>Não é um bean Spring: guarda estado de uma montagem, então cada
 * requisição cria o seu.</p>
 */
public class SorveteBuilder {
    public static final int MAX_COBERTURAS = 3;

    private final CoberturasDisponiveis coberturasDisponiveis;
    private FabricaLinha fabrica;
    private ItemCatalogo sabor;
    private final List<ItemCatalogo> coberturas = new ArrayList<>();

    public SorveteBuilder(CoberturasDisponiveis coberturasDisponiveis) {
        this.coberturasDisponiveis = coberturasDisponiveis;
    }

    public SorveteBuilder daLinha(FabricaLinha fabrica) {
        this.fabrica = fabrica;
        return this;
    }

    public SorveteBuilder comSabor(ItemCatalogo sabor) {
        this.sabor = sabor;
        return this;
    }

    public SorveteBuilder comCobertura(ItemCatalogo cobertura) {
        this.coberturas.add(cobertura);
        return this;
    }

    public Sorvete construir() {
        validar();
        Sorvete sorvete = fabrica.criarSorvete(sabor);                    // Abstract Factory
        for (ItemCatalogo cobertura : coberturas) {
            sorvete = coberturasDisponiveis.aplicar(cobertura, sorvete);  // Decorator
        }
        return sorvete;
    }

    private void validar() {
        if (fabrica == null) {
            throw new ComposicaoInvalidaException("Escolha a linha do sorvete");
        }
        if (sabor == null) {
            throw new ComposicaoInvalidaException("Escolha um sabor");
        }
        if (!sabor.isDoTipo(TipoItem.SABOR)) {
            throw new ComposicaoInvalidaException(sabor.getSku() + " não é um sabor");
        }
        if (!sabor.pertenceA(fabrica.getLinha())) {
            throw new ComposicaoInvalidaException("O sabor " + sabor.getNome()
                    + " não pertence à linha " + fabrica.getLinha().getNome());
        }
        if (coberturas.size() > MAX_COBERTURAS) {
            throw new ComposicaoInvalidaException("Máximo de " + MAX_COBERTURAS + " coberturas por sorvete");
        }
        if (coberturas.stream().map(ItemCatalogo::getSku).distinct().count() < coberturas.size()) {
            throw new ComposicaoInvalidaException("A mesma cobertura não pode ser repetida");
        }
        for (ItemCatalogo cobertura : coberturas) {
            if (!cobertura.isDoTipo(TipoItem.COBERTURA)) {
                throw new ComposicaoInvalidaException(cobertura.getSku() + " não é uma cobertura");
            }
        }
    }
}
