package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Objeto de valor imutável: as condições do cupom no momento em que foi aplicado ao pedido. */
@Embeddable
public class CupomAplicado {
    @Column(name = "cupom_codigo", length = 30)
    private String codigo;

    @Column(name = "cupom_descricao", length = 120)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "cupom_tipo", length = 20)
    private TipoDesconto tipo;

    @Column(name = "cupom_valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "cupom_minimo", precision = 10, scale = 2)
    private BigDecimal valorMinimo;

    protected CupomAplicado() { }

    public CupomAplicado(String codigo, String descricao, TipoDesconto tipo, BigDecimal valor, BigDecimal valorMinimo) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.valor = valor;
        this.valorMinimo = valorMinimo;
    }

    public boolean atendeMinimo(BigDecimal subtotal) { return subtotal.compareTo(valorMinimo) >= 0; }

    /** Desconto sobre o subtotal; zero se o mínimo deixou de ser atendido; nunca maior que o subtotal. */
    public BigDecimal descontoSobre(BigDecimal subtotal) {
        if (!atendeMinimo(subtotal)) return Dinheiro.ZERO;
        return Dinheiro.arredondar(tipo.calcular(subtotal, valor).min(subtotal));
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorMinimo() { return valorMinimo; }
}
