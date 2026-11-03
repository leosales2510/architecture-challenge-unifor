package br.unifor.sorveteria.catalogo.domain;

/**
 * Porta de acesso aos itens do catálogo usada pelas fábricas (DIP): o domínio
 * depende desta abstração, e não do Spring Data / JPA diretamente.
 */
public interface CatalogoItens {
    /** @throws br.unifor.sorveteria.catalogo.domain.exception.RecursoNaoEncontradoException se o SKU não existir ou estiver inativo */
    ItemCatalogo buscar(String sku);
}
