package br.unifor.sorveteria.pedido.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.pedido.domain.Cupom;

public interface CupomRepository extends JpaRepository<Cupom, String> {
    Optional<Cupom> findByCodigoIgnoreCase(String codigo);

    List<Cupom> findByAtivoTrueOrderByCodigo();
}
