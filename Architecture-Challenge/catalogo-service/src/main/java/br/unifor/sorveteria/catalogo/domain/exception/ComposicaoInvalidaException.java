package br.unifor.sorveteria.catalogo.domain.exception;

/** Regra de negócio violada ao montar um sorvete (sabor de outra linha, coberturas demais...). */
public class ComposicaoInvalidaException extends RuntimeException {
    public ComposicaoInvalidaException(String mensagem) { super(mensagem); }
}
