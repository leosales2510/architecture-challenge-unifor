// PADRÃO: MEDIATOR — papel: colega concreto (fala com o pagamento-service).
package br.unifor.sorveteria.pedido.checkout;

import org.springframework.stereotype.Component;

import br.unifor.sorveteria.pedido.client.PagamentoClient;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

@Component
public class ParticipantePagamento extends Participante {
    private final PagamentoClient pagamentos;

    public ParticipantePagamento(PagamentoClient pagamentos) { this.pagamentos = pagamentos; }

    public void cobrar(ContextoCheckout ctx) {
        ctx.setSituacaoPagamento(SituacaoPagamento.EM_ANDAMENTO);
        try {
            Pagamento p = pagamentos.pagar(ctx.getPedidoId(), ctx.getValor(), ctx.getMetodo(), ctx.getNumeroCartao());
            ctx.setPagamentoId(p.id());
            ctx.setSituacaoPagamento(p.aprovado() ? SituacaoPagamento.APROVADO : SituacaoPagamento.RECUSADO);
            if (p.aprovado()) {
                ctx.setCodigoAutorizacao(p.codigoAutorizacao());
                avisar(EventoCheckout.PAGAMENTO_APROVADO, ctx);
            } else {
                ctx.setMensagem(p.motivoRecusa());
                avisar(EventoCheckout.PAGAMENTO_RECUSADO, ctx);
            }
        } catch (DadosPagamentoInvalidosException e) {
            ctx.setSituacaoPagamento(SituacaoPagamento.NAO_COBRADO);
            ctx.setMensagem(e.getMessage());
            avisar(EventoCheckout.DADOS_PAGAMENTO_INVALIDOS, ctx);
        } catch (ServicoIndisponivelException e) {      // comprovadamente não cobrado
            ctx.setSituacaoPagamento(SituacaoPagamento.NAO_COBRADO);
            ctx.setMensagem(e.getMessage());
            avisar(EventoCheckout.FALHA_INTEGRACAO, ctx);
        } catch (PagamentoIncertoException e) {
            ctx.setSituacaoPagamento(SituacaoPagamento.INCERTO);
            ctx.setMensagem("Não foi possível confirmar o resultado do pagamento. O pedido ficou em verificação "
                    + "e não será cobrado novamente.");
            avisar(EventoCheckout.PAGAMENTO_INCERTO, ctx);
        }
    }
}
