package br.unifor.sorveteria.pagamento.domain.exception;

public class DadosPagamentoInvalidosException extends RuntimeException {
    public DadosPagamentoInvalidosException(String mensagem) { super(mensagem); }
}
