package br.unifor.sorveteria.estoque.domain.exception;

public class OperacaoInvalidaException extends RuntimeException {
    public OperacaoInvalidaException(String mensagem) { super(mensagem); }
}
