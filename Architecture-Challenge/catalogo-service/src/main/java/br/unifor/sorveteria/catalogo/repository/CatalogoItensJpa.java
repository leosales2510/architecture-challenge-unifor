package br.unifor.sorveteria.catalogo.repository;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.CatalogoItens;
import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.exception.RecursoNaoEncontradoException;

/** Implementação da porta CatalogoItens sobre o repositório JPA. */
@Component
class CatalogoItensJpa implements CatalogoItens {
    private final ItemCatalogoRepository repository;

    CatalogoItensJpa(ItemCatalogoRepository repository) { this.repository = repository; }

    @Override public ItemCatalogo buscar(String sku) {
        return repository.findBySkuAndAtivoTrue(sku)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado: " + sku));
    }
}
