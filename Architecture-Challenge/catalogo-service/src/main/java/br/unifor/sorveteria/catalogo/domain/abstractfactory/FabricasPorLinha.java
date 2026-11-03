// PADRÃO: ABSTRACT FACTORY — escolhe a fábrica concreta da linha pedida pelo cliente.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.Linha;

/**
 * Substitui o "linha.equalsIgnoreCase("Zero") ? new FabricaZero() : new FabricaTradicional()"
 * do TotemMediator original. Toda FabricaLinha registrada no Spring entra no mapa:
 * uma nova linha exige só uma nova fábrica, sem editar esta classe (Aberto/Fechado).
 */
@Component
public class FabricasPorLinha {
    private final Map<Linha, FabricaLinha> fabricas = new EnumMap<>(Linha.class);

    public FabricasPorLinha(List<FabricaLinha> todas) {
        todas.forEach(f -> fabricas.put(f.getLinha(), f));
    }

    public FabricaLinha para(Linha linha) {
        FabricaLinha fabrica = fabricas.get(linha);
        if (fabrica == null) {
            throw new IllegalStateException("Nenhuma fábrica registrada para a linha " + linha);
        }
        return fabrica;
    }
}
