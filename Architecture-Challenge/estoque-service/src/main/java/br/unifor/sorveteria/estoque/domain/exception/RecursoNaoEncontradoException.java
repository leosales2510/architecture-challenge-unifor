package br.unifor.sorveteria.estoque.domain.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) { super(mensagem); }
}
