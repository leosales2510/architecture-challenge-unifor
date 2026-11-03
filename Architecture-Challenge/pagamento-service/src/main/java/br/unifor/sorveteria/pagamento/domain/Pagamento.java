package br.unifor.sorveteria.pagamento.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Uma tentativa de cobrança. O número do cartão nunca é gravado: apenas os 4 últimos dígitos. */
@Entity
@Table(name = "pagamentos")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusPagamento status;

    @Column(length = 30)
    private String codigoAutorizacao;

    @Column(length = 120)
    private String motivoRecusa;

    @Column(length = 4)
    private String finalCartao;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    protected Pagamento() { }

    public static Pagamento registrar(SolicitacaoPagamento s, ResultadoGateway resultado) {
        Pagamento p = new Pagamento();
        p.pedidoId = s.pedidoId();
        p.valor = s.valor();
        p.metodo = s.metodo();
        p.status = resultado.aprovado() ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO;
        p.codigoAutorizacao = resultado.codigoAutorizacao();
        p.motivoRecusa = resultado.motivo();
        String cartao = s.numeroCartao();
        p.finalCartao = cartao == null || cartao.length() < 4 ? null : cartao.substring(cartao.length() - 4);
        return p;
    }

    public boolean isAprovado() { return status == StatusPagamento.APROVADO; }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public BigDecimal getValor() { return valor; }
    public MetodoPagamento getMetodo() { return metodo; }
    public StatusPagamento getStatus() { return status; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public String getFinalCartao() { return finalCartao; }
    public Instant getCriadoEm() { return criadoEm; }
}
