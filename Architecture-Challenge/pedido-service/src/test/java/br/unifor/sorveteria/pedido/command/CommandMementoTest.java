package br.unifor.sorveteria.pedido.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unifor.sorveteria.pedido.domain.CupomAplicado;
import br.unifor.sorveteria.pedido.domain.ItemPedido;
import br.unifor.sorveteria.pedido.domain.Itens;
import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.SnapshotPedido;
import br.unifor.sorveteria.pedido.domain.TipoDesconto;
import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;

class CommandMementoTest {
    private final Pedido pedido = new Pedido();
    private final HistoricoComandos historico = new HistoricoComandos();

    private void executar(Comando c) {
        c.executar(pedido);
        historico.empilhar(c);
    }

    private void desfazer() { historico.desempilhar().orElseThrow().desfazer(pedido); }

    @Test
    @DisplayName("Reproduz as etapas 2 a 8 da Main.java do projeto original do Grupo 3")
    void roteiroDoProjetoOriginal() {
        ItemPedido morango = Itens.morangoZeroComCaldaEGranulado();
        executar(new AdicionarItemCommand(morango));
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("17.50"));

        executar(new AdicionarItemCommand(Itens.limaoZero(1)));
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("31.00"));

        executar(new AplicarCupomCommand(Itens.sorvete10()));
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("27.90"));

        desfazer(); // cupom
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("31.00"));
        assertThat(pedido.getCupom()).isNull();

        desfazer(); // segundo item
        assertThat(pedido.getItens()).hasSize(1);
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("17.50"));

        executar(new RemoverItemCommand(morango.getCodigo()));
        assertThat(pedido.getItens()).isEmpty();
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("0.00"));

        desfazer(); // remoção
        assertThat(pedido.getItens()).singleElement()
                .satisfies(i -> assertThat(i.getCodigo()).isEqualTo(morango.getCodigo())); // identidade preservada
        assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("17.50"));
        assertThat(historico.estaVazio()).isFalse();
    }

    @Test
    void comandoQueFalhaNaoGuardaBackupNemAlteraOPedido() {
        executar(new AdicionarItemCommand(Itens.limaoZero(1)));
        CupomAplicado exigeMinimo = new CupomAplicado("ZERO5", "R$ 5", TipoDesconto.VALOR_FIXO, new BigDecimal("5"), new BigDecimal("20"));
        AplicarCupomCommand cupom = new AplicarCupomCommand(exigeMinimo);

        assertThatThrownBy(() -> cupom.executar(pedido)).isInstanceOf(CupomInvalidoException.class);
        assertThat(pedido.getCupom()).isNull();
        assertThatThrownBy(() -> cupom.desfazer(pedido)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void historicoEhLifoEDescartaOMaisAntigoAcimaDoLimite() {
        for (int i = 0; i < HistoricoComandos.LIMITE + 5; i++) executar(new AdicionarItemCommand(Itens.limaoZero(1)));

        assertThat(historico.tamanho()).isEqualTo(HistoricoComandos.LIMITE);
        assertThat(historico.nomes().get(0)).startsWith("Adicionar 1x");
    }

    @Test
    @DisplayName("Memento: o snapshot não expõe o estado para fora do pacote do Pedido")
    void snapshotEhOpaco() {
        boolean temMetodoPublico = Arrays.stream(SnapshotPedido.class.getDeclaredMethods())
                .anyMatch(m -> Modifier.isPublic(m.getModifiers()));
        boolean temConstrutorPublico = Arrays.stream(SnapshotPedido.class.getDeclaredConstructors())
                .anyMatch(c -> Modifier.isPublic(c.getModifiers()));

        assertThat(temMetodoPublico).isFalse();
        assertThat(temConstrutorPublico).isFalse();
    }
}
