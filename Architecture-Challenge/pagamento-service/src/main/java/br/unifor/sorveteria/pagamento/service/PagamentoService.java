package br.unifor.sorveteria.pagamento.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.Pagamento;
import br.unifor.sorveteria.pagamento.domain.ResultadoGateway;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.StatusPagamento;
import br.unifor.sorveteria.pagamento.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pagamento.domain.exception.RecursoNaoEncontradoException;
import br.unifor.sorveteria.pagamento.domain.factorymethod.ProcessadorPagamento;
import br.unifor.sorveteria.pagamento.repository.PagamentoRepository;

@Service
@Transactional
public class PagamentoService {
    private final Map<MetodoPagamento, ProcessadorPagamento> processadores = new EnumMap<>(MetodoPagamento.class);
    private final PagamentoRepository repository;
    private final Object[] travas = new Object[64];

    /** Polimorfismo (GRASP): cada processador se registra pelo método que atende; não há if/switch por meio. */
    public PagamentoService(List<ProcessadorPagamento> processadores, PagamentoRepository repository) {
        processadores.forEach(p -> this.processadores.put(p.metodo(), p));
        this.repository = repository;
        for (int i = 0; i < travas.length; i++) travas[i] = new Object();
    }

    /**
     * Idempotência por pedido, também sob concorrência. A verificação "já existe aprovado?",
     * a cobrança e a gravação acontecem com a trava do pedido segura, e a trava só é solta
     * DEPOIS do commit (por isso este método não é transacional: cada chamada ao repositório
     * comita sozinha). Assim a 2ª chamada simultânea espera e encontra o pagamento da 1ª.
     *
     * <p>Limitação: a trava vale para UMA instância do serviço (ver ADR-008 / backlog E-2).</p>
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResultadoProcessamento processar(SolicitacaoPagamento solicitacao) {
        synchronized (travaDo(solicitacao.pedidoId())) {
            // O pedido-service pode repetir a chamada (ex.: timeout na resposta).
            // Um pedido já pago nunca é cobrado duas vezes.
            Optional<Pagamento> jaAprovado = repository.findFirstByPedidoIdAndStatus(solicitacao.pedidoId(), StatusPagamento.APROVADO);
            if (jaAprovado.isPresent()) return new ResultadoProcessamento(jaAprovado.get(), false);

            ProcessadorPagamento processador = processadores.get(solicitacao.metodo());
            if (processador == null) {
                throw new DadosPagamentoInvalidosException("Meio de pagamento não suportado: " + solicitacao.metodo());
            }
            ResultadoGateway resultado = processador.processar(solicitacao);
            return new ResultadoProcessamento(repository.save(Pagamento.registrar(solicitacao, resultado)), true);
        }
    }

    /** Travas "listradas": memória fixa; pedidos diferentes raramente compartilham a mesma trava (só esperam um pouco). */
    private Object travaDo(Long pedidoId) {
        return travas[Math.floorMod(pedidoId.hashCode(), travas.length)];
    }

    @Transactional(readOnly = true)
    public Pagamento buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pagamento> doPedido(Long pedidoId) { return repository.findByPedidoIdOrderByIdDesc(pedidoId); }
}
