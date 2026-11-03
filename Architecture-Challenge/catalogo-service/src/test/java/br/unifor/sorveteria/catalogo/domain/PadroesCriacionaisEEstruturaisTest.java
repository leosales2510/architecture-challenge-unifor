package br.unifor.sorveteria.catalogo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricaLinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricaTradicional;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricaZero;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.FabricasPorLinha;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.Sorvete;
import br.unifor.sorveteria.catalogo.domain.abstractfactory.SorveteZero;
import br.unifor.sorveteria.catalogo.domain.builder.SorveteBuilder;
import br.unifor.sorveteria.catalogo.domain.decorator.CaldaChocolate;
import br.unifor.sorveteria.catalogo.domain.decorator.CoberturasDisponiveis;
import br.unifor.sorveteria.catalogo.domain.decorator.Granulado;
import br.unifor.sorveteria.catalogo.domain.exception.ComposicaoInvalidaException;

class PadroesCriacionaisEEstruturaisTest {
    private final CatalogoEmMemoria catalogo = new CatalogoEmMemoria();
    private final FabricaLinha tradicional = new FabricaTradicional(catalogo);
    private final FabricaLinha zero = new FabricaZero(catalogo);
    private final CoberturasDisponiveis coberturas = new CoberturasDisponiveis();

    private SorveteBuilder builder() { return new SorveteBuilder(coberturas); }

    @Nested
    @DisplayName("Abstract Factory")
    class AbstractFactory {
        @Test
        void cadaFabricaProduzUmaFamiliaConsistente() {
            Sorvete s = zero.criarSorvete(catalogo.buscar("SAB-ZERO-MOR"));

            assertThat(s).isInstanceOf(SorveteZero.class);
            assertThat(s.getCasquinha().getSku()).isEqualTo("CASQ-ZERO");
            assertThat(s.getLinha()).isEqualTo(Linha.ZERO_ACUCAR);
        }

        @Test
        void precoDoSorveteEhBaseMaisCasquinha() {
            assertThat(tradicional.criarSorvete(catalogo.buscar("SAB-TRAD-CHOC")).getPreco()).isEqualByComparingTo("11.00");
            assertThat(zero.criarSorvete(catalogo.buscar("SAB-ZERO-MOR")).getPreco()).isEqualByComparingTo("13.50");
        }

        @Test
        void seletorDevolveAFabricaDaLinha() {
            FabricasPorLinha fabricas = new FabricasPorLinha(List.of(tradicional, zero));
            assertThat(fabricas.para(Linha.ZERO_ACUCAR)).isSameAs(zero);
            assertThat(fabricas.para(Linha.TRADICIONAL)).isSameAs(tradicional);
        }
    }

    @Nested
    @DisplayName("Decorator")
    class Decorator {
        @Test
        void coberturasSaoEmpilhaveisESomamPrecoEDescricao() {
            Sorvete base = zero.criarSorvete(catalogo.buscar("SAB-ZERO-MOR"));
            Sorvete decorado = new Granulado(new CaldaChocolate(base, catalogo.buscar("COB-CALDA")), catalogo.buscar("COB-GRAN"));

            assertThat(decorado.getPreco()).isEqualByComparingTo("17.50");
            assertThat(decorado.getDescricao()).endsWith("+ calda de chocolate + granulado");
            assertThat(decorado.getComponentes()).containsExactly("SAB-ZERO-MOR", "CASQ-ZERO", "COB-CALDA", "COB-GRAN");
            assertThat(decorado.getCasquinha()).isSameAs(base.getCasquinha());
        }

        @Test
        void coberturaSemDecoratorEhRejeitadaEmVezDeIgnorada() {
            Sorvete base = tradicional.criarSorvete(catalogo.buscar("SAB-TRAD-CHOC"));
            assertThatThrownBy(() -> coberturas.aplicar(catalogo.buscar("COB-NOVA"), base))
                    .isInstanceOf(ComposicaoInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void reproduzOExemploDoProjetoOriginal() {
            Sorvete s = builder().daLinha(zero)
                    .comSabor(catalogo.buscar("SAB-ZERO-MOR"))
                    .comCobertura(catalogo.buscar("COB-CALDA"))
                    .comCobertura(catalogo.buscar("COB-GRAN"))
                    .construir();

            assertThat(s.getDescricao()).isEqualTo(
                    "Sorvete zero açúcar de morango na casquinha integral sem açúcar + calda de chocolate + granulado");
            assertThat(s.getPreco()).isEqualTo(new BigDecimal("17.50"));
        }

        @Test
        void exigeLinhaESabor() {
            assertThatThrownBy(() -> builder().comSabor(catalogo.buscar("SAB-ZERO-MOR")).construir())
                    .isInstanceOf(ComposicaoInvalidaException.class).hasMessageContaining("linha");
            assertThatThrownBy(() -> builder().daLinha(zero).construir())
                    .isInstanceOf(ComposicaoInvalidaException.class).hasMessageContaining("sabor");
        }

        @Test
        void rejeitaSaborDeOutraLinha() {
            assertThatThrownBy(() -> builder().daLinha(zero).comSabor(catalogo.buscar("SAB-TRAD-CHOC")).construir())
                    .isInstanceOf(ComposicaoInvalidaException.class)
                    .hasMessageContaining("não pertence à linha Zero Açúcar");
        }

        @Test
        void limitaQuantidadeDeCoberturas() {
            SorveteBuilder b = builder().daLinha(tradicional).comSabor(catalogo.buscar("SAB-TRAD-CHOC"));
            List.of("COB-CALDA", "COB-GRAN", "COB-CHANT", "COB-CAST").forEach(sku -> b.comCobertura(catalogo.buscar(sku)));

            assertThatThrownBy(b::construir).hasMessageContaining("Máximo de 3");
        }

        @Test
        void rejeitaCoberturaRepetidaEItemQueNaoEhCobertura() {
            assertThatThrownBy(() -> builder().daLinha(tradicional).comSabor(catalogo.buscar("SAB-TRAD-CHOC"))
                    .comCobertura(catalogo.buscar("COB-GRAN")).comCobertura(catalogo.buscar("COB-GRAN")).construir())
                    .hasMessageContaining("repetida");
            assertThatThrownBy(() -> builder().daLinha(tradicional).comSabor(catalogo.buscar("SAB-TRAD-CHOC"))
                    .comCobertura(catalogo.buscar("CASQ-TRAD")).construir())
                    .hasMessageContaining("não é uma cobertura");
        }
    }
}
