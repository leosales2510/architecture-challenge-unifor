// PADRÃO: FACTORY METHOD — papel: CREATOR.
package br.unifor.sorveteria.pagamento.domain.factorymethod;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.ResultadoGateway;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;
import br.unifor.sorveteria.pagamento.domain.adapter.GatewayPagamento;

/**
 * O algoritmo de processamento é o mesmo para qualquer meio (validar → obter o
 * gateway → cobrar), mas QUAL gateway é criado é decidido pela subclasse, no
 * método-fábrica {@link #criarGateway()}. Diferente da Abstract Factory do
 * catálogo: aqui há um único produto (GatewayPagamento) e a escolha é feita por
 * herança, não por um objeto-fábrica que cria uma família.
 */
public abstract class ProcessadorPagamento {

    public abstract MetodoPagamento metodo();

    /** Factory Method: cada subclasse decide qual GatewayPagamento concreto instanciar. */
    protected abstract GatewayPagamento criarGateway();

    /** Regras de entrada específicas do meio de pagamento. */
    protected abstract void validar(SolicitacaoPagamento solicitacao);

    public final ResultadoGateway processar(SolicitacaoPagamento solicitacao) {
        validar(solicitacao);
        GatewayPagamento gateway = criarGateway();
        return gateway.cobrar(solicitacao);
    }
}
