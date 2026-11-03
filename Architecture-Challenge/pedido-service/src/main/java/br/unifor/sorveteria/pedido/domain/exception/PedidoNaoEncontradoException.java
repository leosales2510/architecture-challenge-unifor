package br.unifor.sorveteria.pedido.domain.exception;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(String mensagem) { super(mensagem); }
}
