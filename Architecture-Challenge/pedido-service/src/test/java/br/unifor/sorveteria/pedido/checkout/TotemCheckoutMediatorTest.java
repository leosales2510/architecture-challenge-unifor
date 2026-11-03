package br.unifor.sorveteria.pedido.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unifor.sorveteria.pedido.client.EstoqueClient;
import br.unifor.sorveteria.pedido.client.PagamentoClient;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.domain.Falta;
import br.unifor.sorveteria.pedido.domain.Itens;
import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.StatusPedido;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;
import br.unifor.sorveteria.pedido.repository.PedidoRepository;

/** Testa o Mediator com os clientes remotos simulados (Mockito): sem rede, sem Spring. */
class TotemCheckoutMediatorTest {
    private final EstoqueClient estoque = mock(EstoqueClient.class);
    private final PagamentoClient pagamentos = mock(PagamentoClient.class);
    private final PedidoRepository repositorio = mock(PedidoRepository.class);
    private final Pedido pedido = new Pedido();
    private TotemCheckoutMediator mediator;
    private ContextoCheckout ctx;

    @BeforeEach
    void preparar() {
        pedido.adicionar(Itens.morangoZeroComCaldaEGranulado());
        pedido.iniciarCheckout();
        when(repositorio.findById(1L)).thenReturn(Optional.of(pedido));
        mediator = new TotemCheckoutMediator(new ParticipantePedido(repositorio), new ParticipanteEstoque(estoque),
                new ParticipantePagamento(pagamentos));
        ctx = new ContextoCheckout(1L, pedido.getTotal(), pedido.componentesNecessarios(), "CARTAO", "4111111111111111");
    }

    private static Pagamento pagamento(String status, String motivo) {
        return new Pagamento(50L, 1L, new BigDecimal("17.50"), "CARTAO", status, "APROVADO".equals(status) ? "123456" : null, motivo);
    }

    @Test
    void pagamentoAprovadoConfirmaReservaEMarcaPedidoComoPago() {
        when(estoque.reservar(any(), any())).thenReturn(9L);
        when(pagamentos.pagar(any(), any(), anyString(), any())).thenReturn(pagamento("APROVADO", null));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.APROVADO);
        verify(estoque).confirmar(9L);
        verify(estoque, never()).liberar(anyLong());
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getCodigoAutorizacao()).isEqualTo("123456");
        assertThat(ctx.getTrilha()).containsExactly("CHECKOUT_INICIADO", "ESTOQUE_RESERVADO", "PAGAMENTO_APROVADO",
                "ESTOQUE_CONFIRMADO", "PEDIDO_ATUALIZADO");
    }

    @Test
    void pagamentoRecusadoLiberaAReservaCompensacao() {
        when(estoque.reservar(any(), any())).thenReturn(9L);
        when(pagamentos.pagar(any(), any(), anyString(), any())).thenReturn(pagamento("RECUSADO", "Saldo insuficiente"));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.PAGAMENTO_RECUSADO);
        verify(estoque).liberar(9L);
        verify(estoque, never()).confirmar(anyLong());
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGAMENTO_RECUSADO);
        assertThat(pedido.getMensagem()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void estoqueInsuficienteNaoChegaACobrar() {
        when(estoque.reservar(any(), any()))
                .thenThrow(new EstoqueInsuficienteException(List.of(new Falta("COB-CALDA", 1, 0))));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.ESTOQUE_INSUFICIENTE);
        assertThat(ctx.getFaltas()).extracting(Falta::sku).containsExactly("COB-CALDA");
        verify(pagamentos, never()).pagar(any(), any(), any(), any());
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ABERTO);
    }

    @Test
    void pagamentoForaDoArLiberaAReservaEDevolveOPedidoAoCarrinho() {
        when(estoque.reservar(any(), any())).thenReturn(9L);
        when(pagamentos.pagar(any(), any(), anyString(), any()))
                .thenThrow(new ServicoIndisponivelException("pagamentos", new RuntimeException("timeout")));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.FALHA_INTEGRACAO);
        verify(estoque).liberar(9L);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ABERTO);
    }

    @Test
    void pagamentoIncertoMantemAReservaEBloqueiaOPedido() {
        when(estoque.reservar(any(), any())).thenReturn(9L);
        when(pagamentos.pagar(any(), any(), anyString(), any()))
                .thenThrow(new PagamentoIncertoException("timeout de leitura", new RuntimeException()));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.PAGAMENTO_EM_VERIFICACAO);
        assertThat(ctx.getSituacaoPagamento()).isEqualTo(SituacaoPagamento.INCERTO);
        verify(estoque, never()).liberar(anyLong());
        verify(estoque, never()).confirmar(anyLong());
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGAMENTO_EM_VERIFICACAO);
        assertThat(pedido.getReservaId()).isEqualTo(9L);
    }

    @Test
    void estoqueForaDoArNaoCobraNemLibera() {
        when(estoque.reservar(any(), any()))
                .thenThrow(new ServicoIndisponivelException("estoque", new RuntimeException("connection refused")));

        mediator.finalizar(ctx);

        assertThat(ctx.getResultado()).isEqualTo(ResultadoCheckout.FALHA_INTEGRACAO);
        verify(pagamentos, never()).pagar(any(), any(), any(), any());
        verify(estoque, never()).liberar(anyLong());
    }
}
