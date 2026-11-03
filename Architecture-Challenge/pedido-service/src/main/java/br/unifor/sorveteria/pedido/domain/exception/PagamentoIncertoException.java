package br.unifor.sorveteria.pedido.domain.exception;

/**
 * A cobrança pode ter acontecido, mas não sabemos (timeout de leitura, 5xx desconhecido,
 * resposta ilegível). Nunca deve levar a liberar o estoque ou a cobrar de novo automaticamente.
 */
public class PagamentoIncertoException extends RuntimeException {
    public PagamentoIncertoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
