package br.unifor.sorveteria.pedido.checkout;

public enum ResultadoCheckout {
    APROVADO,
    PAGAMENTO_RECUSADO,
    ESTOQUE_INSUFICIENTE,
    DADOS_PAGAMENTO_INVALIDOS,
    PAGAMENTO_EM_VERIFICACAO,
    FALHA_INTEGRACAO
}
