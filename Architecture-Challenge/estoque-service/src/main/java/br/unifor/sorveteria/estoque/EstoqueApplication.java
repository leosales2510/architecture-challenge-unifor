package br.unifor.sorveteria.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microsserviço de Estoque: dono das quantidades e das reservas feitas pelos pedidos. */
@SpringBootApplication
public class EstoqueApplication {
    public static void main(String[] args) {
        SpringApplication.run(EstoqueApplication.class, args);
    }
}
