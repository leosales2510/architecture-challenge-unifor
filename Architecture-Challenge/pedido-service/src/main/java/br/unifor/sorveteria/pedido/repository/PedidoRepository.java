package br.unifor.sorveteria.pedido.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.pedido.domain.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
