// PADRÃO: MEDIATOR — papel: colega concreto (atualiza o próprio Pedido no banco).
package br.unifor.sorveteria.pedido.checkout;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.exception.PedidoNaoEncontradoException;
import br.unifor.sorveteria.pedido.repository.PedidoRepository;

/**
 * Sem @Transactional de propósito: cada findById/save já é transacional no Spring Data,
 * e evitar o proxy garante que o setMediator() chegue ao objeto real.
 */
@Component
public class ParticipantePedido extends Participante {
    private final PedidoRepository pedidos;

    public ParticipantePedido(PedidoRepository pedidos) { this.pedidos = pedidos; }

    public void concluir(ContextoCheckout ctx) {
        atualizar(ctx, p -> {
            p.registrarReserva(ctx.getReservaId());
            p.confirmarPagamento(ctx.getPagamentoId(), ctx.getCodigoAutorizacao());
        });
    }

    public void registrarRecusa(ContextoCheckout ctx) {
        atualizar(ctx, p -> p.registrarRecusa(ctx.getPagamentoId(), ctx.getMensagem()));
    }

    public void marcarEmVerificacao(ContextoCheckout ctx) {
        atualizar(ctx, p -> {
            p.registrarReserva(ctx.getReservaId());     // a reserva é mantida para a reconciliação
            p.marcarPagamentoEmVerificacao(ctx.getMensagem());
        });
    }

    public void devolverParaEdicao(ContextoCheckout ctx) {
        atualizar(ctx, p -> p.devolverParaEdicao(ctx.getMensagem()));
    }

    private void atualizar(ContextoCheckout ctx, Consumer<Pedido> alteracao) {
        Pedido pedido = pedidos.findById(ctx.getPedidoId())
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + ctx.getPedidoId()));
        alteracao.accept(pedido);
        pedidos.save(pedido);
        avisar(EventoCheckout.PEDIDO_ATUALIZADO, ctx);
    }
}
