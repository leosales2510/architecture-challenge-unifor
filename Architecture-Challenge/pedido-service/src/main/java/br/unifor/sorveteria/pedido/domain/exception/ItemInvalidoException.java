package br.unifor.sorveteria.pedido.domain.exception;

/** O catálogo recusou a montagem (sabor de outra linha, cobertura inexistente...). */
public class ItemInvalidoException extends RuntimeException {
    public ItemInvalidoException(String mensagem) { super(mensagem); }
}
