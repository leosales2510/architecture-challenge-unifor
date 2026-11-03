package br.unifor.sorveteria.catalogo.service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unifor.sorveteria.catalogo.api.dto.ComponenteResponse;
import br.unifor.sorveteria.catalogo.api.dto.MontagemRequest;
import br.unifor.sorveteria.catalogo.api.dto.MontagemResponse;
import br.unifor.sorveteria.catalogo.domain.CatalogoItens;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricasPorLinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;
import br.unifor.sorveteria.catalogo.domain.builder.SorveteBuilder;
import br.unifor.sorveteria.catalogo.domain.decorator.CoberturasDisponiveis;

/**
 * Caso de uso "montar sorvete": resolve a fábrica da linha (Abstract Factory),
 * entrega as partes ao Builder e converte o produto decorado em DTO.
 */
@Service
@Transactional(readOnly = true)
public class MontagemService {
    private final FabricasPorLinha fabricas;
    private final CatalogoItens itens;
    private final CoberturasDisponiveis coberturasDisponiveis;

    public MontagemService(FabricasPorLinha fabricas, CatalogoItens itens, CoberturasDisponiveis coberturasDisponiveis) {
        this.fabricas = fabricas;
        this.itens = itens;
        this.coberturasDisponiveis = coberturasDisponiveis;
    }

    public MontagemResponse montar(MontagemRequest request) {
        SorveteBuilder builder = new SorveteBuilder(coberturasDisponiveis)
                .daLinha(fabricas.para(request.linha()))
                .comSabor(itens.buscar(request.saborSku()));
        request.coberturasOuVazio().forEach(sku -> builder.comCobertura(itens.buscar(sku)));

        Sorvete sorvete = builder.construir();

        return new MontagemResponse(sorvete.getLinha(), request.saborSku(), request.coberturasOuVazio(),
                sorvete.getDescricao(), sorvete.getPreco().setScale(2, RoundingMode.HALF_UP),
                agrupar(sorvete.getComponentes()));
    }

    private static List<ComponenteResponse> agrupar(List<String> skus) {
        Map<String, Integer> quantidades = new LinkedHashMap<>();
        skus.forEach(sku -> quantidades.merge(sku, 1, Integer::sum));
        return quantidades.entrySet().stream().map(e -> new ComponenteResponse(e.getKey(), e.getValue())).toList();
    }
}
