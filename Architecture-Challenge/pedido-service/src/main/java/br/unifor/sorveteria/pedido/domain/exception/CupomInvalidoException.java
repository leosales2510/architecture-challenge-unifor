package br.unifor.sorveteria.pedido.domain.exception;

public class CupomInvalidoException extends RuntimeException {
    public CupomInvalidoException(String mensagem) { super(mensagem); }
}
