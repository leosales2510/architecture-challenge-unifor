package br.unifor.sorveteria.pedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microsserviço de Pedidos: carrinho, cupons, desfazer (Command + Memento) e
 * coordenação do checkout com Estoque e Pagamentos (Mediator).
 */
@SpringBootApplication
public class PedidoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidoApplication.class, args);
    }
}
