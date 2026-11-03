package br.unifor.sorveteria.pedido.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import br.unifor.sorveteria.pedido.client.CatalogoClient;
import br.unifor.sorveteria.pedido.client.EstoqueClient;
import br.unifor.sorveteria.pedido.client.PagamentoClient;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.ItemQuantidade;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Montagem;
import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Pagamento;
import br.unifor.sorveteria.pedido.domain.Falta;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoIncertoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;

/**
 * Sobe o pedido-service inteiro (Spring + H2 + Facade + Commands + Mediator) e
 * substitui SOMENTE os clientes HTTP dos outros serviços por mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PedidoApiIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean CatalogoClient catalogo;
    @MockitoBean EstoqueClient estoque;
    @MockitoBean PagamentoClient pagamentos;

    private static final String ITEM_ZERO = """
            {"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"],"quantidade":1}""";

    @BeforeEach
    void catalogoResponde() {
        when(catalogo.montar(eq("ZERO_ACUCAR"), eq("SAB-ZERO-MOR"), anyList())).thenReturn(new Montagem("ZERO_ACUCAR",
                "SAB-ZERO-MOR", List.of("COB-CALDA", "COB-GRAN"), "Sorvete zero açúcar de morango + calda + granulado",
                new BigDecimal("17.50"), List.of(new ItemQuantidade("SAB-ZERO-MOR", 1), new ItemQuantidade("CASQ-ZERO", 1),
                new ItemQuantidade("COB-CALDA", 1), new ItemQuantidade("COB-GRAN", 1))));
        when(estoque.verificar(anyList())).thenReturn(List.of());
    }

    private long novoPedido() throws Exception {
        String json = mvc.perform(post("/api/pedidos")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private ResultActions adicionar(long id, String json) throws Exception {
        return mvc.perform(post("/api/pedidos/{id}/itens", id).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions finalizar(long id, String cartao) throws Exception {
        return mvc.perform(post("/api/pedidos/{id}/finalizacao", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"metodo\":\"CARTAO\",\"numeroCartao\":\"" + cartao + "\"}"));
    }

    @Test
    void fluxoDoCarrinhoComCupomEDesfazer() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO.replace("\"quantidade\":1", "\"quantidade\":2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(35.00))
                .andExpect(jsonPath("$.itens", hasSize(1)));

        mvc.perform(put("/api/pedidos/{id}/cupom", id).contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"sorvete10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desconto").value(3.50))
                .andExpect(jsonPath("$.total").value(31.50))
                .andExpect(jsonPath("$.historicoDesfazer[0]").value("Aplicar cupom SORVETE10"));

        mvc.perform(post("/api/pedidos/{id}/desfazer", id))
                .andExpect(jsonPath("$.total").value(35.00))
                .andExpect(jsonPath("$.cupom").doesNotExist());
        mvc.perform(post("/api/pedidos/{id}/desfazer", id)).andExpect(jsonPath("$.itens", hasSize(0)));
        mvc.perform(post("/api/pedidos/{id}/desfazer", id))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("NADA_PARA_DESFAZER"));
    }

    @Test
    void removerItemEDesfazerARemocao() throws Exception {
        long id = novoPedido();
        String json = adicionar(id, ITEM_ZERO).andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(json, "$.itens[0].codigo");

        mvc.perform(delete("/api/pedidos/{id}/itens/{c}", id, codigo)).andExpect(jsonPath("$.itens", hasSize(0)));
        mvc.perform(post("/api/pedidos/{id}/desfazer", id)).andExpect(jsonPath("$.itens[0].codigo").value(codigo));
        mvc.perform(get("/api/pedidos/{id}/historico", id)).andExpect(jsonPath("$[0]").value("Adicionar 1x Sorvete zero açúcar de morango + calda + granulado"));
        mvc.perform(delete("/api/pedidos/{id}/itens/{c}", id, "nao-existe")).andExpect(status().isNotFound());
    }

    @Test
    void checkoutAprovado() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(10L);
        when(pagamentos.pagar(eq(id), any(), eq("CARTAO"), anyString()))
                .thenReturn(new Pagamento(20L, id, new BigDecimal("17.50"), "CARTAO", "APROVADO", "654321", null));

        finalizar(id, "4111111111111111")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROVADO"))
                .andExpect(jsonPath("$.pedido.status").value("PAGO"))
                .andExpect(jsonPath("$.pedido.codigoAutorizacao").value("654321"))
                .andExpect(jsonPath("$.pedido.reservaId").value(10))
                .andExpect(jsonPath("$.trilha[0]").value("CHECKOUT_INICIADO"));
        verify(estoque).confirmar(10L);

        // pedido pago não aceita mais nada
        adicionar(id, ITEM_ZERO).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_EDITAVEL"));
        finalizar(id, "4111111111111111").andExpect(status().isConflict());
    }

    @Test
    void checkoutComPagamentoRecusadoRetorna402ELiberaEstoque() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(11L);
        when(pagamentos.pagar(eq(id), any(), anyString(), anyString()))
                .thenReturn(new Pagamento(21L, id, new BigDecimal("17.50"), "CARTAO", "RECUSADO", null, "Saldo insuficiente"));

        finalizar(id, "4000000000000002")
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.codigo").value("PAGAMENTO_RECUSADO"))
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente"))
                .andExpect(jsonPath("$.pedido.status").value("PAGAMENTO_RECUSADO"))
                .andExpect(jsonPath("$.pedido.editavel").value(true))
                .andExpect(jsonPath("$.trilha[3]").value("ESTOQUE_LIBERADO"));
        verify(estoque).liberar(11L);
    }

    @Test
    void erroInesperadoNoCheckoutNaoDeixaOPedidoTravado() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenThrow(new IllegalStateException("falha inesperada"));

        finalizar(id, "4111111111111111")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"));
        mvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.editavel").value(true));
        verify(pagamentos, never()).pagar(any(), any(), any(), any());
    }

    @Test
    void pagamentoRecusadoSeguidoDeErroInesperadoDevolveOPedidoAoCliente() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(12L);
        when(pagamentos.pagar(eq(id), any(), anyString(), anyString()))
                .thenReturn(new Pagamento(22L, id, new BigDecimal("17.50"), "CARTAO", "RECUSADO", null, "Saldo insuficiente"));
        doThrow(new IllegalStateException("falha inesperada")).doNothing().when(estoque).liberar(12L);

        finalizar(id, "4000000000000002").andExpect(status().isInternalServerError());

        // recusa comprovada: nada foi cobrado, então é seguro liberar o estoque e reabrir o pedido
        mvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(jsonPath("$.status").value("PAGAMENTO_RECUSADO"))
                .andExpect(jsonPath("$.editavel").value(true));
        verify(estoque, times(2)).liberar(12L);
    }

    @Test
    void pagamentoAprovadoSeguidoDeErroInesperadoConcluiSemLiberarEstoque() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(14L);
        when(pagamentos.pagar(eq(id), any(), anyString(), anyString()))
                .thenReturn(new Pagamento(24L, id, new BigDecimal("17.50"), "CARTAO", "APROVADO", "777888", null));
        doThrow(new IllegalStateException("falha inesperada")).when(estoque).confirmar(14L);

        finalizar(id, "4111111111111111").andExpect(status().isInternalServerError());

        mvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.codigoAutorizacao").value("777888"));
        verify(estoque, never()).liberar(anyLong());
    }

    @Test
    void resultadoIncertoBloqueiaNovaCobrancaEReconciliaSoComAprovacaoComprovada() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(13L);
        when(pagamentos.pagar(eq(id), any(), anyString(), anyString()))
                .thenThrow(new PagamentoIncertoException("Read timed out", new RuntimeException()));

        finalizar(id, "4111111111111111")
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("PAGAMENTO_EM_VERIFICACAO"))
                .andExpect(jsonPath("$.pedido.status").value("PAGAMENTO_EM_VERIFICACAO"))
                .andExpect(jsonPath("$.pedido.editavel").value(false));

        // nem nova cobrança, nem cancelamento, nem liberação automática
        finalizar(id, "4111111111111111").andExpect(status().isConflict());
        mvc.perform(post("/api/pedidos/{id}/cancelamento", id)).andExpect(status().isConflict());
        verify(pagamentos, times(1)).pagar(any(), any(), any(), any());
        verify(estoque, never()).liberar(anyLong());

        // reconciliação sem aprovação comprovada: continua bloqueado
        when(pagamentos.consultarAprovado(id)).thenReturn(Optional.empty());
        mvc.perform(post("/api/pedidos/{id}/reconciliacao", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PAGAMENTO_NAO_CONFIRMADO"));
        mvc.perform(get("/api/pedidos/{id}", id)).andExpect(jsonPath("$.status").value("PAGAMENTO_EM_VERIFICACAO"));

        // reconciliação encontra o pagamento aprovado: conclui e confirma a reserva mantida
        when(pagamentos.consultarAprovado(id))
                .thenReturn(Optional.of(new Pagamento(23L, id, new BigDecimal("17.50"), "CARTAO", "APROVADO", "999000", null)));
        mvc.perform(post("/api/pedidos/{id}/reconciliacao", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.codigoAutorizacao").value("999000"));
        verify(estoque).confirmar(13L);
        verify(estoque, never()).liberar(anyLong());
    }

    @Test
    void erroInesperadoDuranteACobrancaEhTratadoComoIncerto() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        when(estoque.reservar(eq(id), anyList())).thenReturn(15L);
        when(pagamentos.pagar(eq(id), any(), anyString(), anyString())).thenThrow(new IllegalStateException("bug no cliente"));

        finalizar(id, "4111111111111111").andExpect(status().isInternalServerError());

        mvc.perform(get("/api/pedidos/{id}", id)).andExpect(jsonPath("$.status").value("PAGAMENTO_EM_VERIFICACAO"));
        verify(estoque, never()).liberar(anyLong());
        mvc.perform(post("/api/pedidos/{id}/reconciliacao", id + 1000)).andExpect(status().isNotFound());
    }

    @Test
    void estoqueInsuficienteAoAdicionarRetorna409() throws Exception {
        long id = novoPedido();
        when(estoque.verificar(anyList())).thenReturn(List.of(new Falta("COB-GRAN", 1, 0)));

        adicionar(id, ITEM_ZERO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"))
                .andExpect(jsonPath("$.faltas[0].sku").value("COB-GRAN"));
        mvc.perform(get("/api/pedidos/{id}", id)).andExpect(jsonPath("$.itens", hasSize(0)));
    }

    @Test
    void servicoRemotoForaDoArRetorna503() throws Exception {
        long id = novoPedido();
        when(catalogo.montar(anyString(), anyString(), anyList()))
                .thenThrow(new ServicoIndisponivelException("catálogo", new RuntimeException()));

        adicionar(id, ITEM_ZERO)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("SERVICO_INDISPONIVEL"))
                .andExpect(jsonPath("$.servico").value("catálogo"));
    }

    @Test
    void cuponsInvalidosRetornam422() throws Exception {
        long id = novoPedido();
        adicionar(id, ITEM_ZERO);
        for (String cupom : List.of("NAOEXISTE", "VERAO25", "ZERO5")) {
            mvc.perform(put("/api/pedidos/{id}/cupom", id).contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + cupom + "\"}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.codigo").value("CUPOM_INVALIDO"));
        }
        mvc.perform(get("/api/pedidos/{id}", id)).andExpect(jsonPath("$.historicoDesfazer", hasSize(1)));
    }

    @Test
    void validacoesDeEntrada() throws Exception {
        long id = novoPedido();
        adicionar(id, """
                {"linha":"DIET","saborSku":"","quantidade":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.linha").exists())
                .andExpect(jsonPath("$.erros.saborSku").exists())
                .andExpect(jsonPath("$.erros.quantidade").exists());
        mvc.perform(post("/api/pedidos/{id}/finalizacao", id).contentType(MediaType.APPLICATION_JSON).content("{\"metodo\":\"BOLETO\"}"))
                .andExpect(status().isBadRequest());
        finalizar(id, "4111111111111111")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail", startsWith("Não é possível finalizar um pedido vazio")));
        mvc.perform(get("/api/pedidos/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
        mvc.perform(get("/api/rota-inexistente")).andExpect(status().isNotFound());
        mvc.perform(put("/api/pedidos/{id}", id)).andExpect(status().isMethodNotAllowed());
        verify(pagamentos, never()).pagar(any(), any(), any(), any());
    }

    @Test
    void listaCupons() throws Exception {
        mvc.perform(get("/api/cupons")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
    }
}
