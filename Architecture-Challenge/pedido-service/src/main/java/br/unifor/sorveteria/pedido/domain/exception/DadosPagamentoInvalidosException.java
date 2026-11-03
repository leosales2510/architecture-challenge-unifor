package br.unifor.sorveteria.pedido.domain.exception;

public class DadosPagamentoInvalidosException extends RuntimeException {
    public DadosPagamentoInvalidosException(String mensagem) { super(mensagem); }
}
