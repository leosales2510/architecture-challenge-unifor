// PADRÃO: FACADE (novo no Architecture Challenge)
// Papel: interface única e simples para o subsistema de pedidos.
package br.unifor.sorveteria.pedido.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.unifor.sorveteria.pedido.checkout.CheckoutMediator;
import br.unifor.sorveteria.pedido.checkout.ContextoCheckout;
import br.unifor.sorveteria.pedido.checkout.ResultadoCheckout;
import br.unifor.sorveteria.pedido.client.CatalogoClient;
import br.unifor.sorveteria.pedido.client.EstoqueClient;
import br.unifor.sorveteria.pedido.client.PagamentoClient;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Montagem;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.command.AdicionarItemCommand;
import br.unifor.sorveteria.pedido.command.AplicarCupomCommand;
import br.unifor.sorveteria.pedido.command.Comando;
import br.unifor.sorveteria.pedido.command.HistoricoComandos;
import br.unifor.sorveteria.pedido.command.HistoricosPorPedido;
import br.unifor.sorveteria.pedido.command.RemoverItemCommand;
import br.unifor.sorveteria.pedido.domain.Componente;
import br.unifor.sorveteria.pedido.domain.CupomAplicado;
import br.unifor.sorveteria.pedido.domain.Falta;
import br.unifor.sorveteria.pedido.domain.ItemPedido;
import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.StatusPedido;
import br.unifor.sorveteria.pedido.domain.exception.CheckoutNaoConcluidoException;
import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.NadaParaDesfazerException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoNaoConfirmadoException;
import br.unifor.sorveteria.pedido.domain.exception.PedidoNaoEncontradoException;
import br.unifor.sorveteria.pedido.domain.exception.RegraPedidoException;
import br.unifor.sorveteria.pedido.dto.CheckoutResponse;
import br.unifor.sorveteria.pedido.dto.FinalizacaoRequest;
import br.unifor.sorveteria.pedido.dto.NovoItemRequest;
import br.unifor.sorveteria.pedido.dto.PedidoResponse;
import br.unifor.sorveteria.pedido.repository.CupomRepository;
import br.unifor.sorveteria.pedido.repository.PedidoRepository;

/**
 * O controller REST só conhece esta classe. Por trás dela há um subsistema com
 * várias peças que o controller NÃO precisa conhecer nem orquestrar: clientes
 * HTTP do catálogo e do estoque, os Commands e o histórico por pedido (Memento),
 * o repositório JPA, os cupons e o Mediator do checkout. Cada método aqui é um
 * caso de uso do totem, em poucas linhas, delegando o trabalho às peças.
 *
 * <p>Os métodos não são @Transactional de propósito: chamadas HTTP não devem
 * segurar uma transação do banco aberta. A consistência entre chamadas vem do
 * bloqueio otimista (@Version) do Pedido.</p>
 */
@Service
public class PedidoFacade {
    private static final Logger log = LoggerFactory.getLogger(PedidoFacade.class);

    private final PedidoRepository pedidos;
    private final CupomRepository cupons;
    private final HistoricosPorPedido historicos;
    private final CatalogoClient catalogo;
    private final EstoqueClient estoque;
    private final PagamentoClient pagamentos;
    private final CheckoutMediator checkout;
    private final Clock relogio;

    public PedidoFacade(PedidoRepository pedidos, CupomRepository cupons, HistoricosPorPedido historicos,
                        CatalogoClient catalogo, EstoqueClient estoque, PagamentoClient pagamentos, CheckoutMediator checkout,
                        Clock relogio) {
        this.pedidos = pedidos;
        this.cupons = cupons;
        this.historicos = historicos;
        this.catalogo = catalogo;
        this.estoque = estoque;
        this.pagamentos = pagamentos;
        this.checkout = checkout;
        this.relogio = relogio;
    }

    public PedidoResponse criar() {
        return resposta(pedidos.save(new Pedido()));
    }

    public PedidoResponse consultar(Long id) {
        return resposta(carregar(id));
    }

    public PedidoResponse adicionarItem(Long id, NovoItemRequest req) {
        Pedido pedido = carregarEditavel(id);

        Montagem m = catalogo.montar(req.linha(), req.saborSku(), req.coberturasOuVazio());
        ItemPedido item = ItemPedido.novo(m.descricao(), m.linha(), m.saborSku(), m.coberturas(), req.quantidade(),
                m.precoUnitario(), m.componentes().stream().map(c -> new Componente(c.sku(), c.quantidade())).toList());

        verificarEstoque(pedido, item);
        return executar(pedido, new AdicionarItemCommand(item));
    }

    public PedidoResponse removerItem(Long id, String codigoItem) {
        return executar(carregarEditavel(id), new RemoverItemCommand(codigoItem));
    }

    public PedidoResponse aplicarCupom(Long id, String codigo) {
        Pedido pedido = carregarEditavel(id);
        CupomAplicado cupom = cupons.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new CupomInvalidoException("Cupom " + codigo + " não existe"))
                .aplicavelEm(LocalDate.now(relogio));
        return executar(pedido, new AplicarCupomCommand(cupom));
    }

    public PedidoResponse desfazer(Long id) {
        Pedido pedido = carregarEditavel(id);
        HistoricoComandos historico = historicos.de(id);
        Comando ultimo = historico.desempilhar()
                .orElseThrow(() -> new NadaParaDesfazerException("Não há ações para desfazer no pedido " + id));
        try {
            ultimo.desfazer(pedido);                    // Memento: restaura a foto guardada pelo comando
            return resposta(pedidos.save(pedido));
        } catch (RuntimeException e) {
            historico.empilhar(ultimo);                 // não perde o comando se a gravação falhar
            throw e;
        }
    }

    public List<String> historico(Long id) {
        carregar(id);
        return historicos.de(id).nomes();
    }

    public CheckoutResponse finalizar(Long id, FinalizacaoRequest req) {
        Pedido pedido = carregar(id);
        pedido.iniciarCheckout();
        pedido = pedidos.save(pedido); // AGUARDANDO_PAGAMENTO; a @Version barra um segundo clique simultâneo

        ContextoCheckout ctx = new ContextoCheckout(pedido.getId(), pedido.getTotal(), pedido.componentesNecessarios(),
                req.metodo(), req.numeroCartao());
        try {
            checkout.finalizar(ctx);                    // Mediator: estoque -> pagamento -> pedido
        } catch (RuntimeException e) {
            recuperarCheckoutInterrompido(ctx);
            throw e;
        }

        if (ctx.getResultado() == ResultadoCheckout.APROVADO) historicos.descartar(id);
        CheckoutResponse resposta = new CheckoutResponse(ctx.getResultado(), ctx.getMensagem(), ctx.getTrilha(),
                ctx.getFaltas(), resposta(carregar(id)));
        if (ctx.getResultado() != ResultadoCheckout.APROVADO) throw new CheckoutNaoConcluidoException(resposta);
        return resposta;
    }

    /**
     * Reconciliação de um pedido em PAGAMENTO_EM_VERIFICACAO. Estratégia segura: só conclui
     * se o pagamento-service mostrar um pagamento APROVADO. A ausência de aprovação NÃO é
     * tratada como "não cobrado" (a resposta pode estar atrasada): o pedido continua
     * bloqueado e a reserva mantida, para decisão manual.
     */
    public PedidoResponse reconciliarPagamento(Long id) {
        Pedido pedido = carregar(id);
        if (pedido.getStatus() != StatusPedido.PAGAMENTO_EM_VERIFICACAO) {
            throw new RegraPedidoException("Pedido " + id + " está " + pedido.getStatus() + "; não há pagamento em verificação");
        }
        Pagamento aprovado = pagamentos.consultarAprovado(id).orElseThrow(() -> new PagamentoNaoConfirmadoException(
                "Nenhum pagamento aprovado registrado para o pedido " + id + ". A reserva foi mantida; verificação manual necessária"));
        if (pedido.getReservaId() != null) {
            try {
                estoque.confirmar(pedido.getReservaId());
            } catch (RuntimeException e) {
                log.warn("Reserva {} do pedido {} continua PENDENTE: confirmação falhou", pedido.getReservaId(), id, e);
            }
        }
        pedido.confirmarPagamento(aprovado.id(), aprovado.codigoAutorizacao());
        historicos.descartar(id);
        return resposta(pedidos.save(pedido));
    }

    public PedidoResponse cancelar(Long id) {
        Pedido pedido = carregar(id);
        pedido.cancelar();
        historicos.descartar(id);
        return resposta(pedidos.save(pedido));
    }

    // ------------------------------------------------------------------ apoio

    /**
     * Erro inesperado no meio do checkout (exceção fora dos fluxos previstos pelo Mediator).
     * A recuperação depende do que se sabe sobre o dinheiro: só libera o estoque quando
     * comprovadamente nada foi cobrado; se foi aprovado, conclui; se é incerto, bloqueia
     * o pedido em PAGAMENTO_EM_VERIFICACAO até a reconciliação.
     */
    private void recuperarCheckoutInterrompido(ContextoCheckout ctx) {
        try {
            Pedido pedido = carregar(ctx.getPedidoId());
            if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) return; // o Mediator já concluiu
            switch (ctx.getSituacaoPagamento()) {
                case NAO_INICIADO, NAO_COBRADO -> {
                    liberarReserva(ctx);
                    pedido.devolverParaEdicao("Erro inesperado no checkout; nada foi cobrado. Tente novamente");
                }
                case RECUSADO -> {
                    liberarReserva(ctx);
                    pedido.registrarRecusa(ctx.getPagamentoId(), ctx.getMensagem());
                }
                case APROVADO -> {                          // cobrança feita: conclui, nunca libera o estoque
                    pedido.registrarReserva(ctx.getReservaId());
                    pedido.confirmarPagamento(ctx.getPagamentoId(), ctx.getCodigoAutorizacao());
                    historicos.descartar(pedido.getId());
                }
                case EM_ANDAMENTO, INCERTO -> {
                    pedido.registrarReserva(ctx.getReservaId());
                    pedido.marcarPagamentoEmVerificacao("Resultado do pagamento desconhecido; pedido em verificação");
                }
            }
            pedidos.save(pedido);
        } catch (RuntimeException e) {
            log.error("Não foi possível recuperar o checkout do pedido {}", ctx.getPedidoId(), e);
        }
    }

    /** Só é chamado quando nada foi cobrado; se falhar, a reserva fica PENDENTE (limitação conhecida, B21). */
    private void liberarReserva(ContextoCheckout ctx) {
        if (ctx.getReservaId() == null) return;
        try {
            estoque.liberar(ctx.getReservaId());
        } catch (RuntimeException e) {
            log.error("Reserva {} do pedido {} ficou PENDENTE", ctx.getReservaId(), ctx.getPedidoId(), e);
        }
    }

    private PedidoResponse executar(Pedido pedido, Comando comando) {
        comando.executar(pedido);                       // Command: salva o memento e age no receptor
        Pedido salvo = pedidos.save(pedido);
        historicos.de(salvo.getId()).empilhar(comando); // só entra no histórico se executou e gravou
        return resposta(salvo);
    }

    /** Consulta o estoque considerando o que já está no carrinho para os mesmos SKUs. */
    private void verificarEstoque(Pedido pedido, ItemPedido novo) {
        Set<String> skusDoNovo = novo.componentesTotais().stream().map(Componente::sku).collect(Collectors.toSet());
        Map<String, Integer> necessario = new TreeMap<>();
        pedido.componentesNecessarios().forEach(c -> necessario.merge(c.sku(), c.quantidade(), Integer::sum));
        novo.componentesTotais().forEach(c -> necessario.merge(c.sku(), c.quantidade(), Integer::sum));

        List<Componente> consulta = necessario.entrySet().stream().filter(e -> skusDoNovo.contains(e.getKey()))
                .map(e -> new Componente(e.getKey(), e.getValue())).toList();
        List<Falta> faltas = estoque.verificar(consulta);
        if (!faltas.isEmpty()) throw new EstoqueInsuficienteException(faltas);
    }

    private Pedido carregar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    /** Falha cedo, antes de chamar outros serviços, se o pedido não aceita mais alterações. */
    private Pedido carregarEditavel(Long id) {
        Pedido pedido = carregar(id);
        if (!pedido.isEditavel()) {
            throw new RegraPedidoException("Pedido " + id + " está " + pedido.getStatus() + " e não pode ser alterado");
        }
        return pedido;
    }

    private PedidoResponse resposta(Pedido pedido) {
        return PedidoResponse.de(pedido, historicos.de(pedido.getId()).nomes());
    }
}
