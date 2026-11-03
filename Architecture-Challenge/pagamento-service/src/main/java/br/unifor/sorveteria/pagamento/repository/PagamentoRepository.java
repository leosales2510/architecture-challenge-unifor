package br.unifor.sorveteria.pagamento.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.pagamento.domain.Pagamento;
import br.unifor.sorveteria.pagamento.domain.StatusPagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findFirstByPedidoIdAndStatus(Long pedidoId, StatusPagamento status);

    List<Pagamento> findByPedidoIdOrderByIdDesc(Long pedidoId);
}
