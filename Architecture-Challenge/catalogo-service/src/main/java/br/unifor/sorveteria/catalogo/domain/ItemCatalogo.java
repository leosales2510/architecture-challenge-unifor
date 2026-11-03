package br.unifor.sorveteria.catalogo.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Item cadastrado no catálogo. O SKU é o identificador compartilhado com o
 * serviço de Estoque (que guarda apenas SKU + quantidade).
 */
@Entity
@Table(name = "itens_catalogo")
public class ItemCatalogo {

    @Id
    @Column(length = 40)
    private String sku;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItem tipo;

    /** Nulo para itens que servem às duas linhas (as coberturas). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Linha linha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo = true;

    protected ItemCatalogo() { }

    public ItemCatalogo(String sku, String nome, TipoItem tipo, Linha linha, BigDecimal preco) {
        this.sku = sku;
        this.nome = nome;
        this.tipo = tipo;
        this.linha = linha;
        this.preco = preco;
    }

    /** Information Expert: o próprio item sabe se pode compor um sorvete da linha informada. */
    public boolean pertenceA(Linha outra) {
        return linha == null || linha == outra;
    }

    public boolean isDoTipo(TipoItem outro) { return tipo == outro; }

    public String getSku() { return sku; }
    public String getNome() { return nome; }
    public TipoItem getTipo() { return tipo; }
    public Linha getLinha() { return linha; }
    public BigDecimal getPreco() { return preco; }
    public boolean isAtivo() { return ativo; }
}
