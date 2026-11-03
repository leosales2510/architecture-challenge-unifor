package br.unifor.sorveteria.pedido.domain.exception;

public class NadaParaDesfazerException extends RuntimeException {
    public NadaParaDesfazerException(String mensagem) { super(mensagem); }
}
