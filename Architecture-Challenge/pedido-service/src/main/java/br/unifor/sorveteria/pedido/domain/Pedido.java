// PADRÕES: MEMENTO (Originator) + COMMAND (Receiver) — herdados do projeto do Grupo 3.
package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.ItemNaoEncontradoException;
import br.unifor.sorveteria.pedido.domain.exception.RegraPedidoException;

/**
 * Agregado Pedido. É o ORIGINATOR do Memento (cria e restaura SnapshotPedido) e o
 * RECEIVER dos Commands (quem realmente executa adicionar/remover/aplicar cupom).
 * Também é o Information Expert dos totais e das transições de status.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusPedido status = StatusPedido.ABERTO;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    @OrderColumn(name = "posicao")
    private List<ItemPedido> itens = new ArrayList<>();

    @Embedded
    private CupomAplicado cupom;

    private Long reservaId;
    private Long pagamentoId;

    @Column(length = 30)
    private String codigoAutorizacao;

    /** Último aviso do checkout (motivo da recusa, falta de estoque...), exibido no totem. */
    @Column(length = 255)
    private String mensagem;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    private Instant atualizadoEm;

    /** Bloqueio otimista: impede, por exemplo, que dois cliques em "finalizar" cobrem duas vezes. */
    @Version
    private long versao;

    // ------------------------------------------------------------------ receiver dos comandos

    public void adicionar(ItemPedido item) {
        exigirEditavel();
        itens.add(item);
    }

    public void remover(String codigoItem) {
        exigirEditavel();
        if (!itens.removeIf(i -> i.getCodigo().equals(codigoItem))) {
            throw new ItemNaoEncontradoException("Item " + codigoItem + " não está no pedido " + id);
        }
    }

    public void aplicarCupom(CupomAplicado novo) {
        exigirEditavel();
        if (!novo.atendeMinimo(getSubtotal())) {
            throw new CupomInvalidoException("Cupom " + novo.getCodigo() + " exige pedido mínimo de R$ "
                    + novo.getValorMinimo());
        }
        this.cupom = novo;
    }

    // ------------------------------------------------------------------ memento

    public SnapshotPedido criarSnapshot() { return new SnapshotPedido(itens, cupom); }

    public void restaurar(SnapshotPedido snapshot) {
        exigirEditavel();
        itens.clear();
        snapshot.itens().forEach(copia -> itens.add(copia.recriar()));
        this.cupom = snapshot.cupom();
    }

    // ------------------------------------------------------------------ totais (Information Expert)

    public BigDecimal getSubtotal() {
        return Dinheiro.arredondar(itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getDesconto() { return cupom == null ? Dinheiro.ZERO : cupom.descontoSobre(getSubtotal()); }

    public BigDecimal getTotal() { return getSubtotal().subtract(getDesconto()); }

    /** Tudo o que o pedido consome do estoque, somado por SKU (ordem estável). */
    public List<Componente> componentesNecessarios() {
        Map<String, Integer> porSku = new TreeMap<>();
        itens.forEach(i -> i.componentesTotais().forEach(c -> porSku.merge(c.sku(), c.quantidade(), Integer::sum)));
        return porSku.entrySet().stream().map(e -> new Componente(e.getKey(), e.getValue())).toList();
    }

    // ------------------------------------------------------------------ ciclo de vida do checkout

    public void iniciarCheckout() {
        exigirEditavel();
        if (itens.isEmpty()) throw new RegraPedidoException("Não é possível finalizar um pedido vazio");
        status = StatusPedido.AGUARDANDO_PAGAMENTO;
        mensagem = null;
    }

    public void registrarReserva(Long reservaId) { this.reservaId = reservaId; }

    public void confirmarPagamento(Long pagamentoId, String codigoAutorizacao) {
        if (status != StatusPedido.PAGAMENTO_EM_VERIFICACAO) exigirStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        this.status = StatusPedido.PAGO;
        this.pagamentoId = pagamentoId;
        this.codigoAutorizacao = codigoAutorizacao;
    }

    public void registrarRecusa(Long pagamentoId, String motivo) {
        exigirStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        this.status = StatusPedido.PAGAMENTO_RECUSADO;
        this.pagamentoId = pagamentoId;
        this.reservaId = null;
        this.mensagem = motivo;
    }

    /** Resultado financeiro desconhecido: mantém a reserva e bloqueia o pedido até a reconciliação. */
    public void marcarPagamentoEmVerificacao(String motivo) {
        exigirStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        this.status = StatusPedido.PAGAMENTO_EM_VERIFICACAO;
        this.mensagem = motivo;
    }

    /** Checkout interrompido antes da cobrança (falta de estoque, serviço fora do ar): volta ao carrinho. */
    public void devolverParaEdicao(String motivo) {
        exigirStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        this.status = StatusPedido.ABERTO;
        this.reservaId = null;
        this.mensagem = motivo;
    }

    public void cancelar() {
        exigirEditavel();
        status = StatusPedido.CANCELADO;
    }

    public boolean isEditavel() { return status.isEditavel(); }

    private void exigirEditavel() {
        if (!isEditavel()) throw new RegraPedidoException("Pedido " + id + " está " + status + " e não pode ser alterado");
    }

    private void exigirStatus(StatusPedido esperado) {
        if (status != esperado) throw new RegraPedidoException("Pedido " + id + " está " + status + ", esperado " + esperado);
    }

    @PreUpdate
    void aoAtualizar() { atualizadoEm = Instant.now(); }

    public Long getId() { return id; }
    public StatusPedido getStatus() { return status; }
    public List<ItemPedido> getItens() { return List.copyOf(itens); }
    public CupomAplicado getCupom() { return cupom; }
    public Long getReservaId() { return reservaId; }
    public Long getPagamentoId() { return pagamentoId; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
    public String getMensagem() { return mensagem; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
