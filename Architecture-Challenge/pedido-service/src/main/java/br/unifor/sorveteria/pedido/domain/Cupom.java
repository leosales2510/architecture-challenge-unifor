package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;

/** Cupom cadastrado. Ao ser aplicado, suas condições são copiadas para o pedido (CupomAplicado). */
@Entity
@Table(name = "cupons")
public class Cupom {
    @Id
    @Column(length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDesconto tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMinimo = BigDecimal.ZERO;

    private LocalDate validoAte;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Cupom() { }

    public Cupom(String codigo, String descricao, TipoDesconto tipo, BigDecimal valor, BigDecimal valorMinimo, LocalDate validoAte) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.valor = valor;
        this.valorMinimo = valorMinimo;
        this.validoAte = validoAte;
    }

    /** Information Expert: o cupom verifica a própria validade e gera as condições aplicáveis. */
    public CupomAplicado aplicavelEm(LocalDate hoje) {
        if (!ativo) throw new CupomInvalidoException("Cupom " + codigo + " está desativado");
        if (validoAte != null && hoje.isAfter(validoAte)) {
            throw new CupomInvalidoException("Cupom " + codigo + " expirou em " + validoAte);
        }
        return new CupomAplicado(codigo, descricao, tipo, valor, valorMinimo);
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public TipoDesconto getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getValorMinimo() { return valorMinimo; }
    public LocalDate getValidoAte() { return validoAte; }
    public boolean isAtivo() { return ativo; }
}
