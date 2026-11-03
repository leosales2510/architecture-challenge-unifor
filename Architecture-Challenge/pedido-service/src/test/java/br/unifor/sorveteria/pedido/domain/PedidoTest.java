package br.unifor.sorveteria.pedido.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.ItemNaoEncontradoException;
import br.unifor.sorveteria.pedido.domain.exception.RegraPedidoException;

class PedidoTest {

    @Test
    void calculaSubtotalComQuantidadeETotalComBigDecimal() {
        Pedido p = new Pedido();
        p.adicionar(Itens.morangoZeroComCaldaEGranulado());
        p.adicionar(Itens.limaoZero(2));

        assertThat(p.getSubtotal()).isEqualTo(new BigDecimal("44.50"));
        assertThat(p.getTotal()).isEqualTo(new BigDecimal("44.50"));
    }

    @Test
    void cupomPercentualArredondaMeioParaCima() {
        Pedido p = new Pedido();
        p.adicionar(Itens.limaoZero(3));       // 40,50
        p.aplicarCupom(Itens.sorvete10());     // 4,05

        assertThat(p.getDesconto()).isEqualTo(new BigDecimal("4.05"));
        assertThat(p.getTotal()).isEqualTo(new BigDecimal("36.45"));
    }

    @Test
    void cupomDeValorFixoExigeMinimoEDeixaDeValerSeOSubtotalCair() {
        Pedido p = new Pedido();
        p.adicionar(Itens.limaoZero(1));
        assertThatThrownBy(() -> p.aplicarCupom(Itens.zero5())).isInstanceOf(CupomInvalidoException.class);

        ItemPedido segundo = Itens.limaoZero(1);
        p.adicionar(segundo);                  // 27,00
        p.aplicarCupom(Itens.zero5());
        assertThat(p.getTotal()).isEqualTo(new BigDecimal("22.00"));

        p.remover(segundo.getCodigo());        // 13,50 < mínimo
        assertThat(p.getDesconto()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void cupomNuncaDeixaOTotalNegativo() {
        Pedido p = new Pedido();
        p.adicionar(Itens.limaoZero(2));
        p.aplicarCupom(new CupomAplicado("X", "x", TipoDesconto.VALOR_FIXO, new BigDecimal("999"), BigDecimal.ZERO));
        assertThat(p.getTotal()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void cupomExpiradoOuDesativadoEhRecusado() {
        Cupom verao = new Cupom("VERAO25", "25%", TipoDesconto.PERCENTUAL, new BigDecimal("25"), BigDecimal.ZERO,
                LocalDate.of(2026, 3, 20));
        assertThatThrownBy(() -> verao.aplicavelEm(LocalDate.of(2026, 10, 29)))
                .isInstanceOf(CupomInvalidoException.class).hasMessageContaining("expirou");
        assertThat(verao.aplicavelEm(LocalDate.of(2026, 3, 20)).getCodigo()).isEqualTo("VERAO25");
    }

    @Test
    void somaComponentesPorSkuParaReservarNoEstoque() {
        Pedido p = new Pedido();
        p.adicionar(Itens.morangoZeroComCaldaEGranulado());
        p.adicionar(Itens.limaoZero(2));

        assertThat(p.componentesNecessarios()).contains(new Componente("CASQ-ZERO", 3), new Componente("SAB-ZERO-LIM", 2));
    }

    @Test
    void removerItemInexistenteFalha() {
        assertThatThrownBy(() -> new Pedido().remover("nao-existe")).isInstanceOf(ItemNaoEncontradoException.class);
    }

    @Test
    void quantidadeForaDoLimiteEhRecusada() {
        assertThatThrownBy(() -> Itens.limaoZero(11)).isInstanceOf(RegraPedidoException.class);
        assertThatThrownBy(() -> Itens.limaoZero(0)).isInstanceOf(RegraPedidoException.class);
    }

    @Test
    void cicloDeVidaDoCheckout() {
        Pedido p = new Pedido();
        assertThatThrownBy(p::iniciarCheckout).hasMessageContaining("vazio");

        p.adicionar(Itens.limaoZero(1));
        p.iniciarCheckout();
        assertThat(p.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        assertThatThrownBy(() -> p.adicionar(Itens.limaoZero(1))).isInstanceOf(RegraPedidoException.class);

        p.registrarRecusa(7L, "Saldo insuficiente");
        assertThat(p.isEditavel()).isTrue();             // pode trocar o meio de pagamento ou editar
        assertThat(p.getMensagem()).isEqualTo("Saldo insuficiente");

        p.iniciarCheckout();
        p.confirmarPagamento(8L, "123456");
        assertThat(p.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThatThrownBy(p::cancelar).isInstanceOf(RegraPedidoException.class);
    }
}
