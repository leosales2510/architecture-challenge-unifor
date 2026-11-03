// PADRÃO: COMMAND (herdado do projeto do Grupo 3) — papel: interface do comando.
package br.unifor.sorveteria.pedido.command;

import br.unifor.sorveteria.pedido.domain.Pedido;

/**
 * A ação sobre o carrinho vira um objeto que sabe executar e desfazer.
 *
 * <p>Adaptação para REST: no projeto original o comando guardava a referência ao
 * Pedido em memória. Aqui cada requisição recarrega o Pedido do banco, então o
 * receptor é entregue ao comando em executar/desfazer. O comando continua guardando
 * os PARÂMETROS da ação e o MEMENTO necessário para desfazê-la.</p>
 */
public interface Comando {
    void executar(Pedido receptor);

    void desfazer(Pedido receptor);

    String getNome();
}
