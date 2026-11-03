package br.unifor.sorveteria.pedido.client;

import java.math.BigDecimal;
import java.util.Optional;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;

/** Porta para o pagamento-service. */
public interface PagamentoClient {
    /** Recusa NÃO é exceção: vem como Pagamento com status RECUSADO. */
    /**
     * @throws br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException quando comprovadamente nada foi cobrado
     * @throws br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException quando a cobrança pode ter ocorrido
     */
    Pagamento pagar(Long pedidoId, BigDecimal valor, String metodo, String numeroCartao);

    /** Reconciliação: pagamento APROVADO registrado para o pedido, se existir. */
    Optional<Pagamento> consultarAprovado(Long pedidoId);
}
