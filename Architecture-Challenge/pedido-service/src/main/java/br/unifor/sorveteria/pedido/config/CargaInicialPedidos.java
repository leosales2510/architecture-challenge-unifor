package br.unifor.sorveteria.pedido.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.unifor.sorveteria.pedido.domain.Cupom;
import br.unifor.sorveteria.pedido.domain.TipoDesconto;
import br.unifor.sorveteria.pedido.repository.CupomRepository;

@Configuration
class CargaInicialPedidos {

    @Bean
    Clock relogio() { return Clock.systemDefaultZone(); }

    /** SORVETE10 é o cupom do projeto original; os outros exercitam valor fixo, mínimo e validade. */
    @Bean
    ApplicationRunner cuponsIniciais(CupomRepository cupons, Clock relogio) {
        return (ApplicationArguments args) -> {
            if (cupons.count() > 0) return;
            LocalDate hoje = LocalDate.now(relogio);
            cupons.saveAll(List.of(
                    new Cupom("SORVETE10", "10% de desconto", TipoDesconto.PERCENTUAL, new BigDecimal("10"), BigDecimal.ZERO, null),
                    new Cupom("ZERO5", "R$ 5,00 off em pedidos a partir de R$ 20,00", TipoDesconto.VALOR_FIXO,
                            new BigDecimal("5.00"), new BigDecimal("20.00"), null),
                    new Cupom("VERAO25", "25% de desconto (campanha encerrada)", TipoDesconto.PERCENTUAL,
                            new BigDecimal("25"), BigDecimal.ZERO, hoje.minusDays(1))));
        };
    }
}
