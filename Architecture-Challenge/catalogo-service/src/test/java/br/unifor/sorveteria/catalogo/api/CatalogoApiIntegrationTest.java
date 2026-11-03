package br.unifor.sorveteria.catalogo.api;

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

/** Sobe o serviço inteiro (H2 + carga inicial) e exercita a API REST pela camada HTTP. */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogoApiIntegrationTest {
    @Autowired MockMvc mvc;

    private org.springframework.test.web.servlet.ResultActions montar(String json) throws Exception {
        return mvc.perform(post("/api/catalogo/montagens").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void listaAsDuasLinhasComSaboresECoberturas() throws Exception {
        mvc.perform(get("/api/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linhas", hasSize(2)))
                .andExpect(jsonPath("$.linhas[1].codigo").value("ZERO_ACUCAR"))
                .andExpect(jsonPath("$.linhas[1].casquinha.sku").value("CASQ-ZERO"))
                .andExpect(jsonPath("$.coberturas", hasSize(4)))
                .andExpect(jsonPath("$.maxCoberturas").value(3));
    }

    @Test
    void montaSorveteComPrecoEComponentes() throws Exception {
        montar("""
                {"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoUnitario").value(17.50))
                .andExpect(jsonPath("$.componentes", hasSize(4)))
                .andExpect(jsonPath("$.componentes[0].sku").value("SAB-ZERO-MOR"));
    }

    @Test
    void saborDeOutraLinhaRetorna422() throws Exception {
        montar("""
                {"linha":"ZERO_ACUCAR","saborSku":"SAB-TRAD-CHOC"}""")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("COMPOSICAO_INVALIDA"));
    }

    @Test
    void skuInexistenteRetorna404() throws Exception {
        montar("""
                {"linha":"TRADICIONAL","saborSku":"SAB-NAO-EXISTE"}""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ITEM_NAO_ENCONTRADO"));
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        montar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros.linha").exists())
                .andExpect(jsonPath("$.erros.saborSku").exists());
    }

    @Test
    void linhaDesconhecidaRetorna400() throws Exception {
        montar("""
                {"linha":"DIET","saborSku":"SAB-ZERO-MOR"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
    }
}
