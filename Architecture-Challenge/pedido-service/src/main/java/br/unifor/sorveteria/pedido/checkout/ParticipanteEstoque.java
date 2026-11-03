// PADRÃO: MEDIATOR — papel: colega concreto (fala com o estoque-service).
package br.unifor.sorveteria.pedido.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.unifor.sorveteria.pedido.client.EstoqueClient;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

@Component
public class ParticipanteEstoque extends Participante {
    private static final Logger log = LoggerFactory.getLogger(ParticipanteEstoque.class);
    private final EstoqueClient estoque;

    public ParticipanteEstoque(EstoqueClient estoque) { this.estoque = estoque; }

    public void reservar(ContextoCheckout ctx) {
        try {
            ctx.setReservaId(estoque.reservar(ctx.getPedidoId(), ctx.getComponentes()));
            avisar(EventoCheckout.ESTOQUE_RESERVADO, ctx);
        } catch (EstoqueInsuficienteException e) {
            ctx.setFaltas(e.getFaltas());
            ctx.setMensagem(e.getMessage());
            avisar(EventoCheckout.ESTOQUE_INSUFICIENTE, ctx);
        } catch (ServicoIndisponivelException e) {
            ctx.setMensagem(e.getMessage());
            avisar(EventoCheckout.FALHA_INTEGRACAO, ctx);
        }
    }

    public void confirmar(ContextoCheckout ctx) {
        try {
            estoque.confirmar(ctx.getReservaId());
            avisar(EventoCheckout.ESTOQUE_CONFIRMADO, ctx);
        } catch (ServicoIndisponivelException e) {
            log.warn("Reserva {} do pedido {} ficou PENDENTE: confirmação falhou", ctx.getReservaId(), ctx.getPedidoId(), e);
            avisar(EventoCheckout.ESTOQUE_PENDENTE, ctx);
        }
    }

    /** Compensação: devolve ao estoque o que foi reservado. */
    public void liberar(ContextoCheckout ctx) {
        try {
            estoque.liberar(ctx.getReservaId());
            avisar(EventoCheckout.ESTOQUE_LIBERADO, ctx);
        } catch (ServicoIndisponivelException e) {
            log.warn("Reserva {} do pedido {} ficou PENDENTE: liberação falhou", ctx.getReservaId(), ctx.getPedidoId(), e);
            avisar(EventoCheckout.ESTOQUE_PENDENTE, ctx);
        }
    }
}
