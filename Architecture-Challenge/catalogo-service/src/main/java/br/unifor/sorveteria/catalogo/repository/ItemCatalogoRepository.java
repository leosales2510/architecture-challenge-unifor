package br.unifor.sorveteria.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.domain.Linha;
import br.unifor.sorveteria.catalogo.domain.TipoItem;

public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, String> {
    Optional<ItemCatalogo> findBySkuAndAtivoTrue(String sku);

    List<ItemCatalogo> findByTipoAndAtivoTrueOrderByNome(TipoItem tipo);

    List<ItemCatalogo> findByTipoAndLinhaAndAtivoTrueOrderByNome(TipoItem tipo, Linha linha);
}
