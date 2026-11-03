package br.unifor.sorveteria.pagamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.StatusPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor;
import br.unifor.sorveteria.pagamento.domain.adapter.legado.LegacyCardProcessor.LegacyAuthResponse;
import br.unifor.sorveteria.pagamento.repository.PagamentoRepository;

/** Duas cobranças simultâneas do MESMO pedido, com a operadora demorando para responder. */
@SpringBootTest
class PagamentoConcorrenteTest {
    @Autowired PagamentoService service;
    @Autowired PagamentoRepository repository;
    @MockitoBean LegacyCardProcessor operadora;

    @Test
    void chamadasSimultaneasDoMesmoPedidoCobramUmaUnicaVez() throws Exception {
        when(operadora.authorize(anyString(), anyLong(), anyString())).thenAnswer(inv -> {
            Thread.sleep(300); // operadora lenta: abre a janela de corrida
            return new LegacyAuthResponse("00", "123456");
        });
        SolicitacaoPagamento s = new SolicitacaoPagamento(500L, new BigDecimal("17.50"), MetodoPagamento.CARTAO, "4111111111111111");
        CountDownLatch largada = new CountDownLatch(1);
        Callable<ResultadoProcessamento> chamada = () -> { largada.await(); return service.processar(s); };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<ResultadoProcessamento>> r = List.of(pool.submit(chamada), pool.submit(chamada));
        largada.countDown();
        ResultadoProcessamento a = r.get(0).get(), b = r.get(1).get();
        pool.shutdown();

        verify(operadora, times(1)).authorize(anyString(), anyLong(), anyString());
        assertThat(a.pagamento().getId()).isEqualTo(b.pagamento().getId());
        assertThat(List.of(a.novo(), b.novo())).containsExactlyInAnyOrder(true, false);
        assertThat(repository.findByPedidoIdOrderByIdDesc(500L)).hasSize(1)
                .allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(StatusPagamento.APROVADO));
    }
}
