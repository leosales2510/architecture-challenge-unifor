package br.unifor.sorveteria.pedido.domain.exception;

public class ItemNaoEncontradoException extends RuntimeException {
    public ItemNaoEncontradoException(String mensagem) { super(mensagem); }
}
