// PADRÃO: DECORATOR — registro que liga o SKU cadastrado ao decorator concreto.
package br.unifor.sorveteria.catalogo.domain.decorator;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;
import br.unifor.sorveteria.catalogo.domain.exception.ComposicaoInvalidaException;

/**
 * Substitui o switch ("calda" -> new CaldaChocolate(item); default -> item) do
 * TotemMediator original, que ignorava silenciosamente coberturas desconhecidas.
 * Aqui um SKU sem decorator é erro de composição.
 */
@Component
public class CoberturasDisponiveis {
    private final Map<String, BiFunction<Sorvete, ItemCatalogo, Sorvete>> decorators = Map.of(
            "COB-CALDA", CaldaChocolate::new,
            "COB-GRAN", Granulado::new,
            "COB-CHANT", Chantilly::new,
            "COB-CAST", CastanhaCaramelizada::new);

    public Sorvete aplicar(ItemCatalogo cobertura, Sorvete sorvete) {
        BiFunction<Sorvete, ItemCatalogo, Sorvete> decorator = decorators.get(cobertura.getSku());
        if (decorator == null) {
            throw new ComposicaoInvalidaException("Cobertura sem montagem definida: " + cobertura.getSku());
        }
        return decorator.apply(sorvete, cobertura);
    }
}
