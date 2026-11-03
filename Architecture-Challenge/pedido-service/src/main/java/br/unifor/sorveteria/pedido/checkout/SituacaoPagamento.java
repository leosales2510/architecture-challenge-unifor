package br.unifor.sorveteria.pedido.checkout;

/** O que se sabe, com certeza, sobre o dinheiro numa tentativa de checkout. Decide a recuperação de falhas. */
public enum SituacaoPagamento {
    /** A cobrança ainda não foi solicitada. */
    NAO_INICIADO,
    /** A requisição de cobrança foi enviada e ainda não há resposta. */
    EM_ANDAMENTO,
    /** O pagamento-service respondeu APROVADO. */
    APROVADO,
    /** O pagamento-service respondeu RECUSADO. */
    RECUSADO,
    /** Comprovadamente nada foi cobrado (dados inválidos, conexão recusada, operadora indisponível informada pelo serviço). */
    NAO_COBRADO,
    /** Resposta perdida ou ilegível: a cobrança pode ter ocorrido. */
    INCERTO
}
