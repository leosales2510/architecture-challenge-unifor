// PADRÃO: MEDIATOR — papel: mediador concreto (coordena estoque, pagamento e pedido no checkout).
package br.unifor.sorveteria.pedido.checkout;

import org.springframework.stereotype.Component;

/**
 * Evolução do TotemMediator original. Lá ele coordenava os painéis do totem
 * (cardápio, carrinho, total); essa coordenação de tela agora é do front-end.
 * No back-end o ponto onde vários componentes precisariam se conhecer é o
 * checkout: o estoque precisa saber do pagamento (para liberar a reserva) e o
 * pedido precisa saber dos dois. Sem o mediador seriam dependências cruzadas
 * entre os três; com ele, cada participante só conhece o mediador, e toda a
 * regra "o que acontece depois de X" fica numa tabela de decisão neste método.
 *
 * <p>É também o orquestrador da saga de checkout: as ações de liberação da
 * reserva são as transações de compensação.</p>
 */
@Component
public class TotemCheckoutMediator implements CheckoutMediator {
    private final ParticipantePedido pedido;
    private final ParticipanteEstoque estoque;
    private final ParticipantePagamento pagamento;

    public TotemCheckoutMediator(ParticipantePedido pedido, ParticipanteEstoque estoque, ParticipantePagamento pagamento) {
        this.pedido = registrar(pedido);
        this.estoque = registrar(estoque);
        this.pagamento = registrar(pagamento);
    }

    private <T extends Participante> T registrar(T participante) {
        participante.setMediator(this);
        return participante;
    }

    @Override public void finalizar(ContextoCheckout ctx) {
        ctx.registrar("CHECKOUT_INICIADO");
        estoque.reservar(ctx);
    }

    @Override public void notificar(Participante remetente, EventoCheckout evento, ContextoCheckout ctx) {
        ctx.registrar(evento.name());
        switch (evento) {
            case ESTOQUE_RESERVADO -> pagamento.cobrar(ctx);
            case ESTOQUE_INSUFICIENTE -> {
                ctx.concluir(ResultadoCheckout.ESTOQUE_INSUFICIENTE, ctx.getMensagem());
                pedido.devolverParaEdicao(ctx);
            }
            case PAGAMENTO_APROVADO -> {
                ctx.concluir(ResultadoCheckout.APROVADO, "Pagamento aprovado");
                estoque.confirmar(ctx);
                pedido.concluir(ctx);
            }
            case PAGAMENTO_RECUSADO -> {
                ctx.concluir(ResultadoCheckout.PAGAMENTO_RECUSADO, ctx.getMensagem());
                estoque.liberar(ctx);
                pedido.registrarRecusa(ctx);
            }
            case DADOS_PAGAMENTO_INVALIDOS -> {
                ctx.concluir(ResultadoCheckout.DADOS_PAGAMENTO_INVALIDOS, ctx.getMensagem());
                estoque.liberar(ctx);
                pedido.devolverParaEdicao(ctx);
            }
            case PAGAMENTO_INCERTO -> {                 // NUNCA libera o estoque: o dinheiro pode ter saído
                ctx.concluir(ResultadoCheckout.PAGAMENTO_EM_VERIFICACAO, ctx.getMensagem());
                pedido.marcarEmVerificacao(ctx);
            }
            case FALHA_INTEGRACAO -> {
                ctx.concluir(ResultadoCheckout.FALHA_INTEGRACAO, ctx.getMensagem());
                if (ctx.getReservaId() != null) estoque.liberar(ctx);
                pedido.devolverParaEdicao(ctx);
            }
            case ESTOQUE_CONFIRMADO, ESTOQUE_LIBERADO, ESTOQUE_PENDENTE, PEDIDO_ATUALIZADO -> {
                // fim de um ramo: só fica registrado na trilha
            }
        }
    }
}
