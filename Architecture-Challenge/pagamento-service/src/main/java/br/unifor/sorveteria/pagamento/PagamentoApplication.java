package br.unifor.sorveteria.pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microsserviço de Pagamentos: processa (de forma simulada) cartão e Pix e guarda o histórico. */
@SpringBootApplication
public class PagamentoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PagamentoApplication.class, args);
    }
}
