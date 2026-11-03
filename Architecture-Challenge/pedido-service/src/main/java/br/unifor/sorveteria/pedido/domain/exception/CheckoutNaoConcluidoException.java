package br.unifor.sorveteria.pedido.domain.exception;

import br.unifor.sorveteria.pedido.dto.CheckoutResponse;

/** O checkout terminou sem pagamento aprovado. Carrega o resultado completo para a resposta de erro. */
public class CheckoutNaoConcluidoException extends RuntimeException {
    private final CheckoutResponse checkout;

    public CheckoutNaoConcluidoException(CheckoutResponse checkout) {
        super(checkout.mensagem());
        this.checkout = checkout;
    }

    public CheckoutResponse getCheckout() { return checkout; }
}
