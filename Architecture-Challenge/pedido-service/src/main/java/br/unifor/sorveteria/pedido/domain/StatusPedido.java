package br.unifor.sorveteria.pedido.domain;

public enum StatusPedido {
    /** Carrinho aberto: aceita itens, cupom e desfazer. */
    ABERTO(true),
    /** Checkout em andamento (estoque reservado / cobrança em curso). Bloqueia alterações. */
    AGUARDANDO_PAGAMENTO(false),
    /** A cobrança foi recusada: o cliente pode tentar outro meio ou continuar editando. */
    PAGAMENTO_RECUSADO(true),
    /** O resultado da cobrança é desconhecido: bloqueado até a reconciliação (nem nova cobrança, nem liberação de estoque). */
    PAGAMENTO_EM_VERIFICACAO(false),
    PAGO(false),
    CANCELADO(false);

    private final boolean editavel;

    StatusPedido(boolean editavel) { this.editavel = editavel; }

    public boolean isEditavel() { return editavel; }
}
