package br.unifor.sorveteria.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.unifor.sorveteria.estoque.api.dto.ItemQuantidade;
import br.unifor.sorveteria.estoque.domain.Falta;
import br.unifor.sorveteria.estoque.domain.Reserva;
import br.unifor.sorveteria.estoque.domain.StatusReserva;
import br.unifor.sorveteria.estoque.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.estoque.domain.exception.OperacaoInvalidaException;

/** Cada teste roda numa transação desfeita ao final: o saldo da carga inicial é sempre o ponto de partida. */
@SpringBootTest
@Transactional
class EstoqueServiceTest {
    @Autowired EstoqueService service;

    private int saldo(String sku) { return service.buscar(sku).getQuantidade(); }

    @Test
    void reservaEhTudoOuNada() {
        List<ItemQuantidade> pedido = List.of(new ItemQuantidade("CASQ-ZERO", 5), new ItemQuantidade("SAB-ZERO-LIM", 5));

        assertThatThrownBy(() -> service.reservar(1L, pedido))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .satisfies(e -> assertThat(((EstoqueInsuficienteException) e).getFaltas())
                        .containsExactly(new Falta("SAB-ZERO-LIM", 5, 3)));
        assertThat(saldo("CASQ-ZERO")).isEqualTo(30); // nada foi baixado
    }

    @Test
    void reservaBaixaSaldoSomandoSkusRepetidos() {
        Reserva r = service.reservar(2L, List.of(new ItemQuantidade("CASQ-TRAD", 2), new ItemQuantidade("CASQ-TRAD", 1)));

        assertThat(r.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(r.getItens()).hasSize(1);
        assertThat(saldo("CASQ-TRAD")).isEqualTo(27);
    }

    @Test
    void liberarDevolveSaldoEEhIdempotente() {
        Reserva r = service.reservar(3L, List.of(new ItemQuantidade("COB-GRAN", 4)));
        service.liberar(r.getId());
        service.liberar(r.getId());

        assertThat(saldo("COB-GRAN")).isEqualTo(25);
        assertThat(service.buscarReserva(r.getId()).getStatus()).isEqualTo(StatusReserva.LIBERADA);
    }

    @Test
    void reservaLiberadaNaoPodeSerConfirmadaEConfirmadaNaoPodeSerLiberada() {
        Reserva liberada = service.reservar(4L, List.of(new ItemQuantidade("COB-CALDA", 1)));
        service.liberar(liberada.getId());
        assertThatThrownBy(() -> service.confirmar(liberada.getId())).isInstanceOf(OperacaoInvalidaException.class);

        Reserva confirmada = service.reservar(5L, List.of(new ItemQuantidade("COB-CALDA", 1)));
        service.confirmar(confirmada.getId());
        assertThatThrownBy(() -> service.liberar(confirmada.getId())).isInstanceOf(OperacaoInvalidaException.class);
        assertThat(saldo("COB-CALDA")).isEqualTo(24);
    }

    @Test
    void novaTentativaDoMesmoPedidoSubstituiAReservaPendente() {
        Reserva primeira = service.reservar(6L, List.of(new ItemQuantidade("CASQ-ZERO", 10)));
        Reserva segunda = service.reservar(6L, List.of(new ItemQuantidade("CASQ-ZERO", 4)));

        assertThat(service.buscarReserva(primeira.getId()).getStatus()).isEqualTo(StatusReserva.LIBERADA);
        assertThat(segunda.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(saldo("CASQ-ZERO")).isEqualTo(26);
    }

    @Test
    void skuNaoCadastradoApareceComoFalta() {
        assertThat(service.faltas(List.of(new ItemQuantidade("NAO-EXISTE", 1))))
                .containsExactly(new Falta("NAO-EXISTE", 1, 0));
    }
}
