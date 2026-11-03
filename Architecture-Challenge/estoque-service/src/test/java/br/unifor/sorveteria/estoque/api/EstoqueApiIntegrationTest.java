package br.unifor.sorveteria.estoque.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstoqueApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void reservaCriadaRetorna201ELiberacaoDevolveSaldo() throws Exception {
        String corpo = mvc.perform(post("/api/estoque/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":10,"itens":[{"sku":"SAB-TRAD-CHOC","quantidade":2}]}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(corpo, "$.id");
        mvc.perform(get("/api/estoque/SAB-TRAD-CHOC")).andExpect(jsonPath("$.quantidade").value(18));

        mvc.perform(delete("/api/estoque/reservas/{id}", id)).andExpect(jsonPath("$.status").value("LIBERADA"));
        mvc.perform(get("/api/estoque/SAB-TRAD-CHOC")).andExpect(jsonPath("$.quantidade").value(20));
    }

    @Test
    void estoqueInsuficienteRetorna409ComFaltas() throws Exception {
        mvc.perform(post("/api/estoque/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":11,"itens":[{"sku":"COB-CAST","quantidade":3}]}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"))
                .andExpect(jsonPath("$.faltas[0].sku").value("COB-CAST"))
                .andExpect(jsonPath("$.faltas[0].disponivel").value(2));
    }

    @Test
    void consultaDeDisponibilidadeNaoAlteraSaldo() throws Exception {
        mvc.perform(post("/api/estoque/disponibilidade").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"sku":"SAB-ZERO-LIM","quantidade":4}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false))
                .andExpect(jsonPath("$.faltas[0].solicitado").value(4));
    }

    @Test
    void validacaoDeEntradaRetorna400() throws Exception {
        mvc.perform(post("/api/estoque/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"sku":"COB-CAST","quantidade":0}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros.pedidoId").exists());
        mvc.perform(put("/api/estoque/COB-CAST").contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skuInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/estoque/XYZ")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }
}
