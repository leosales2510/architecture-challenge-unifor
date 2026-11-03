package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import br.unifor.sorveteria.pedido.domain.exception.RegraPedidoException;

/**
 * Linha do carrinho. Guarda uma CÓPIA dos dados do catálogo no momento da compra
 * (descrição, preço, componentes): se o preço mudar depois, o pedido não muda.
 * O "codigo" é a identidade estável do item, preservada quando o Memento é restaurado.
 */
@Entity
@Table(name = "itens_pedido")
public class ItemPedido {
    public static final int MAX_QUANTIDADE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String linha;

    @Column(nullable = false, length = 40)
    private String saborSku;

    @Convert(converter = ConversoresJpa.ListaDeTexto.class)
    @Column(length = 255)
    private List<String> coberturas;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Convert(converter = ConversoresJpa.ListaDeComponentes.class)
    @Column(nullable = false, length = 500)
    private List<Componente> componentes;

    protected ItemPedido() { }

    ItemPedido(String codigo, String descricao, String linha, String saborSku, List<String> coberturas,
               int quantidade, BigDecimal precoUnitario, List<Componente> componentes) {
        if (quantidade < 1 || quantidade > MAX_QUANTIDADE) {
            throw new RegraPedidoException("Quantidade por item deve ser de 1 a " + MAX_QUANTIDADE);
        }
        this.codigo = codigo;
        this.descricao = descricao;
        this.linha = linha;
        this.saborSku = saborSku;
        this.coberturas = List.copyOf(coberturas);
        this.quantidade = quantidade;
        this.precoUnitario = Dinheiro.arredondar(precoUnitario);
        this.componentes = List.copyOf(componentes);
    }

    /** Creator (GRASP): o item nasce a partir dos dados que o catálogo devolveu. */
    public static ItemPedido novo(String descricao, String linha, String saborSku, List<String> coberturas,
                                  int quantidade, BigDecimal precoUnitario, List<Componente> componentes) {
        return new ItemPedido(UUID.randomUUID().toString(), descricao, linha, saborSku, coberturas,
                quantidade, precoUnitario, componentes);
    }

    /** Information Expert: o item conhece preço e quantidade, então calcula o próprio subtotal. */
    public BigDecimal getSubtotal() { return precoUnitario.multiply(BigDecimal.valueOf(quantidade)); }

    /** Componentes multiplicados pela quantidade do item (o que será reservado no estoque). */
    public List<Componente> componentesTotais() {
        return componentes.stream().map(c -> new Componente(c.sku(), c.quantidade() * quantidade)).toList();
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public String getLinha() { return linha; }
    public String getSaborSku() { return saborSku; }
    public List<String> getCoberturas() { return coberturas; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public List<Componente> getComponentes() { return componentes; }
}
