package br.unifor.sorveteria.pagamento.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class PagamentoApiIntegrationTest {
    @Autowired MockMvc mvc;

    private ResultActions pagar(String json) throws Exception {
        return mvc.perform(post("/api/pagamentos").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void cartaoAprovadoRetorna201ENaoGuardaNumeroCompleto() throws Exception {
        pagar("""
                {"pedidoId":100,"valor":17.50,"metodo":"CARTAO","numeroCartao":"4111 1111 1111 1111"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.finalCartao").value("1111"))
                .andExpect(jsonPath("$.numeroCartao").doesNotExist());
    }

    @Test
    void cartaoRecusadoTambemEhRegistrado() throws Exception {
        pagar("""
                {"pedidoId":101,"valor":17.50,"metodo":"CARTAO","numeroCartao":"4000000000000002"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECUSADO"))
                .andExpect(jsonPath("$.motivoRecusa").value("Saldo insuficiente"));
        mvc.perform(get("/api/pagamentos").param("pedidoId", "101")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void pedidoJaPagoNaoEhCobradoDeNovo() throws Exception {
        String json = """
                {"pedidoId":102,"valor":9.90,"metodo":"PIX"}""";
        pagar(json).andExpect(status().isCreated());
        pagar(json).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APROVADO"));
        mvc.perform(get("/api/pagamentos").param("pedidoId", "102")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void operadoraForaDoArRetorna503() throws Exception {
        pagar("""
                {"pedidoId":103,"valor":10,"metodo":"CARTAO","numeroCartao":"4000000000009999"}""")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("GATEWAY_INDISPONIVEL"));
    }

    @Test
    void entradasInvalidas() throws Exception {
        pagar("""
                {"pedidoId":104,"valor":0,"metodo":"CARTAO"}""")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros.valor").exists());
        pagar("""
                {"pedidoId":104,"valor":10,"metodo":"CARTAO"}""")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("DADOS_PAGAMENTO_INVALIDOS"));
        pagar("""
                {"pedidoId":104,"valor":10,"metodo":"BOLETO"}""")
                .andExpect(status().isBadRequest());
    }
}
