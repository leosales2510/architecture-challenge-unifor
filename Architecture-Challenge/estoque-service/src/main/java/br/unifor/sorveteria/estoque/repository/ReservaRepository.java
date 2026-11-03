package br.unifor.sorveteria.estoque.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.estoque.domain.Reserva;
import br.unifor.sorveteria.estoque.domain.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByPedidoIdAndStatus(Long pedidoId, StatusReserva status);
}
