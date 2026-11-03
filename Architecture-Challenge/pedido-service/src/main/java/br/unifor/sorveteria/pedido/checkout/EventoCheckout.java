// PADRÃO: MEDIATOR — eventos que os participantes comunicam ao mediador.
package br.unifor.sorveteria.pedido.checkout;

/** Substitui as Strings ("linha:Zero", "adicionar"...) do TotemMediator original: o compilador verifica os eventos. */
public enum EventoCheckout {
    ESTOQUE_RESERVADO,
    ESTOQUE_INSUFICIENTE,
    ESTOQUE_CONFIRMADO,
    ESTOQUE_LIBERADO,
    /** A chamada de confirmação/liberação falhou; a reserva fica pendente (aviso registrado). */
    ESTOQUE_PENDENTE,
    PAGAMENTO_APROVADO,
    PAGAMENTO_RECUSADO,
    DADOS_PAGAMENTO_INVALIDOS,
    /** Resposta do pagamento perdida/ilegível: a cobrança pode ter ocorrido. */
    PAGAMENTO_INCERTO,
    FALHA_INTEGRACAO,
    PEDIDO_ATUALIZADO
}
