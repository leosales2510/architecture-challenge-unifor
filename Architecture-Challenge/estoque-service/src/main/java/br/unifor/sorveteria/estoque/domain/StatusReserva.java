package br.unifor.sorveteria.estoque.domain;

public enum StatusReserva {
    /** Quantidades já retiradas do saldo, aguardando o resultado do pagamento. */
    PENDENTE,
    /** Pagamento aprovado: a baixa é definitiva. */
    CONFIRMADA,
    /** Pagamento recusado ou falha: as quantidades voltaram ao saldo. */
    LIBERADA
}
