// ==============================================================================
// INICIO DO PADRAO: MEMENTO + COMMAND
// Papel desta classe: Originator do Memento e receptor dos comandos
// ==============================================================================
package sorveteria.memento;

import java.util.ArrayList;
import java.util.List;

import sorveteria.abstractfactory.Sorvete;

/**
 * ORIGINATOR do Memento e, ao mesmo tempo, RECEPTOR dos Commands.
 * E o ponto de encontro dos dois padroes.
 */
public class Pedido {
    private final List<Sorvete> itens = new ArrayList<>();
    private double desconto = 0.0;

    public void adicionar(Sorvete s) { itens.add(s); }

    public void remover(Sorvete s) { itens.remove(s); }

    public void removerUltimo() {
        if (!itens.isEmpty()) itens.remove(itens.size() - 1);
    }

    public void aplicarCupom(String codigo) {
        if ("SORVETE10".equalsIgnoreCase(codigo)) desconto = 0.10;
        else desconto = 0.0;
    }

    public List<Sorvete> getItens() { return new ArrayList<>(itens); }

    public double getDesconto() { return desconto; }

    public double getSubtotal() {
        double t = 0;
        for (Sorvete s : itens) t += s.getPreco();
        return t;
    }

    public double getTotal() { return getSubtotal() * (1 - desconto); }

    /** Cria o Memento com o estado atual. */
    public SnapshotPedido criarSnapshot() { return new SnapshotPedido(itens, desconto); }

    /** Restaura o estado a partir do Memento. */
    public void restaurar(SnapshotPedido snapshot) {
        itens.clear();
        itens.addAll(snapshot.getItens());
        this.desconto = snapshot.getDesconto();
    }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEMENTO + COMMAND  (arquivo Pedido.java)
