package br.unifor.sorveteria.pagamento.domain.exception;

/** O provedor externo não respondeu: não se sabe se houve cobrança, então nada é registrado como aprovado. */
public class GatewayIndisponivelException extends RuntimeException {
    public GatewayIndisponivelException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
