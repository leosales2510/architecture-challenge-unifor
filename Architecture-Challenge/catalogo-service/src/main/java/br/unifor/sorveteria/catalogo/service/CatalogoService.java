package br.unifor.sorveteria.catalogo.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unifor.sorveteria.catalogo.api.dto.CatalogoResponse;
import br.unifor.sorveteria.catalogo.api.dto.ItemResponse;
import br.unifor.sorveteria.catalogo.api.dto.LinhaResponse;
import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;
import br.unifor.sorveteria.catalogo.domain.TipoItem;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Casquinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricasPorLinha;
import br.unifor.sorveteria.catalogo.domain.builder.SorveteBuilder;
import br.unifor.sorveteria.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.unifor.sorveteria.catalogo.repository.ItemCatalogoRepository;

/** Consulta do cardápio exibido no totem. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {
    private final ItemCatalogoRepository repository;
    private final FabricasPorLinha fabricas;

    public CatalogoService(ItemCatalogoRepository repository, FabricasPorLinha fabricas) {
        this.repository = repository;
        this.fabricas = fabricas;
    }

    public CatalogoResponse cardapio() {
        List<LinhaResponse> linhas = Arrays.stream(Linha.values()).map(this::linha).toList();
        List<ItemResponse> coberturas = repository.findByTipoAndAtivoTrueOrderByNome(TipoItem.COBERTURA)
                .stream().map(ItemResponse::de).toList();
        return new CatalogoResponse(linhas, coberturas, SorveteBuilder.MAX_COBERTURAS);
    }

    public ItemResponse item(String sku) {
        return repository.findBySkuAndAtivoTrue(sku).map(ItemResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado: " + sku));
    }

    private LinhaResponse linha(Linha linha) {
        ItemCatalogo base = repository.findByTipoAndLinhaAndAtivoTrueOrderByNome(TipoItem.BASE, linha).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Linha sem preço base cadastrado: " + linha));
        Casquinha casquinha = fabricas.para(linha).criarCasquinha();
        List<ItemResponse> sabores = repository.findByTipoAndLinhaAndAtivoTrueOrderByNome(TipoItem.SABOR, linha)
                .stream().map(ItemResponse::de).toList();
        return new LinhaResponse(linha, linha.getNome(), base.getPreco(),
                new ItemResponse(casquinha.getSku(), casquinha.getDescricao(), casquinha.getPreco()), sabores);
    }
}
