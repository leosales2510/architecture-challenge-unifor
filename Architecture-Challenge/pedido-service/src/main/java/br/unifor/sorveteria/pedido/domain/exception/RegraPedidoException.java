package br.unifor.sorveteria.pedido.domain.exception;

/** Regra de negócio do pedido violada (ex.: alterar pedido já pago, carrinho vazio). */
public class RegraPedidoException extends RuntimeException {
    public RegraPedidoException(String mensagem) { super(mensagem); }
}
