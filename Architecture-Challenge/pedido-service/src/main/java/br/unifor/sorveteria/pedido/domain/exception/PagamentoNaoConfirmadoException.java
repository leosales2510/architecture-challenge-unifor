package br.unifor.sorveteria.pedido.domain.exception;

/** A reconciliação não encontrou pagamento aprovado: o pedido continua em verificação (decisão manual). */
public class PagamentoNaoConfirmadoException extends RuntimeException {
    public PagamentoNaoConfirmadoException(String mensagem) { super(mensagem); }
}
