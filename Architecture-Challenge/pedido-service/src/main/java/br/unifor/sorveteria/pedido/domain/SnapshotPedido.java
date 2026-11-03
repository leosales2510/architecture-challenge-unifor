// PADRÃO: MEMENTO (herdado do projeto do Grupo 3) — papel: MEMENTO.
package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * "Foto" do estado editável do Pedido (itens + cupom). Construtor e getters são
 * package-private: só o Pedido (mesmo pacote) cria e lê o conteúdo. Os comandos e
 * o histórico guardam o snapshot, mas não conseguem enxergar o que há dentro.
 */
public final class SnapshotPedido {
    private final List<ItemCopia> itens;
    private final CupomAplicado cupom;

    SnapshotPedido(List<ItemPedido> itensAtuais, CupomAplicado cupom) {
        this.itens = itensAtuais.stream().map(ItemCopia::de).toList(); // cópia: o snapshot não muda depois
        this.cupom = cupom; // CupomAplicado é imutável
    }

    List<ItemCopia> itens() { return itens; }
    CupomAplicado cupom() { return cupom; }

    /** Cópia imutável de um item, independente da entidade JPA. */
    record ItemCopia(String codigo, String descricao, String linha, String saborSku, List<String> coberturas,
                     int quantidade, BigDecimal precoUnitario, List<Componente> componentes) {
        static ItemCopia de(ItemPedido i) {
            return new ItemCopia(i.getCodigo(), i.getDescricao(), i.getLinha(), i.getSaborSku(), i.getCoberturas(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getComponentes());
        }

        ItemPedido recriar() {
            return new ItemPedido(codigo, descricao, linha, saborSku, coberturas, quantidade, precoUnitario, componentes);
        }
    }
}
