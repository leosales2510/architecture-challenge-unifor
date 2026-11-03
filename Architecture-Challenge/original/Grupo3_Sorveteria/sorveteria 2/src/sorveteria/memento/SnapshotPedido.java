// ==============================================================================
// INICIO DO PADRAO: MEMENTO
// Papel desta classe: O memento: foto do estado do Pedido
// ==============================================================================
package sorveteria.memento;

import java.util.ArrayList;
import java.util.List;

import sorveteria.abstractfactory.Sorvete;

/**
 * MEMENTO.
 * Guarda uma "foto" do estado do Pedido. Os getters sao package-private:
 * so o Pedido (mesmo pacote) le o conteudo. O Historico guarda o comando,
 * mas nunca enxerga o que existe dentro do snapshot.
 */
public class SnapshotPedido {
    private final List<Sorvete> itens;
    private final double desconto;

    SnapshotPedido(List<Sorvete> itens, double desconto) {
        this.itens = new ArrayList<>(itens); // copia: o snapshot nao pode mudar depois
        this.desconto = desconto;
    }

    List<Sorvete> getItens() { return new ArrayList<>(itens); }
    double getDesconto() { return desconto; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEMENTO  (arquivo SnapshotPedido.java)
