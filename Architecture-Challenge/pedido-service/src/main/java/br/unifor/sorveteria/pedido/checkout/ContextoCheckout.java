package br.unifor.sorveteria.pedido.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.unifor.sorveteria.pedido.domain.Componente;
import br.unifor.sorveteria.pedido.domain.Falta;

/**
 * Estado de UMA tentativa de checkout. Viaja junto com cada notificação, o que
 * mantém o mediador e os participantes sem estado (são beans singleton do Spring).
 */
public class ContextoCheckout {
    private final Long pedidoId;
    private final BigDecimal valor;
    private final List<Componente> componentes;
    private final String metodo;
    private final String numeroCartao;

    private SituacaoPagamento situacaoPagamento = SituacaoPagamento.NAO_INICIADO;
    private Long reservaId;
    private Long pagamentoId;
    private String codigoAutorizacao;
    private ResultadoCheckout resultado;
    private String mensagem;
    private List<Falta> faltas = List.of();
    private final List<String> trilha = new ArrayList<>();

    public ContextoCheckout(Long pedidoId, BigDecimal valor, List<Componente> componentes, String metodo, String numeroCartao) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.componentes = List.copyOf(componentes);
        this.metodo = metodo;
        this.numeroCartao = numeroCartao;
    }

    void registrar(String passo) { trilha.add(passo); }

    void concluir(ResultadoCheckout resultado, String mensagem) {
        this.resultado = resultado;
        this.mensagem = mensagem;
    }

    public Long getPedidoId() { return pedidoId; }
    public BigDecimal getValor() { return valor; }
    public List<Componente> getComponentes() { return componentes; }
    public String getMetodo() { return metodo; }
    public String getNumeroCartao() { return numeroCartao; }
    public SituacaoPagamento getSituacaoPagamento() { return situacaoPagamento; }
    void setSituacaoPagamento(SituacaoPagamento situacao) { this.situacaoPagamento = situacao; }
    public Long getReservaId() { return reservaId; }
    void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public Long getPagamentoId() { return pagamentoId; }
    void setPagamentoId(Long pagamentoId) { this.pagamentoId = pagamentoId; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
    void setCodigoAutorizacao(String codigoAutorizacao) { this.codigoAutorizacao = codigoAutorizacao; }
    public ResultadoCheckout getResultado() { return resultado; }
    public String getMensagem() { return mensagem; }
    void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public List<Falta> getFaltas() { return faltas; }
    void setFaltas(List<Falta> faltas) { this.faltas = List.copyOf(faltas); }
    /** Sequência de eventos/ações do checkout, devolvida ao totem para mostrar o Mediator em ação. */
    public List<String> getTrilha() { return List.copyOf(trilha); }
}
