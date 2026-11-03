package br.unifor.sorveteria.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import br.unifor.sorveteria.estoque.domain.exception.OperacaoInvalidaException;

/**
 * Quantidade disponível de um SKU. O estoque não conhece preço nem descrição
 * comercial: esses dados pertencem ao catálogo (cada serviço é dono dos seus dados).
 */
@Entity
@Table(name = "itens_estoque")
public class ItemEstoque {
    @Id
    @Column(length = 40)
    private String sku;

    @Column(nullable = false, length = 80)
    private String descricao;

    @Column(nullable = false)
    private int quantidade;

    /** Bloqueio otimista: duas reservas simultâneas do mesmo SKU não podem gerar saldo negativo. */
    @Version
    private long versao;

    protected ItemEstoque() { }

    public ItemEstoque(String sku, String descricao, int quantidade) {
        this.sku = sku;
        this.descricao = descricao;
        definirQuantidade(quantidade);
    }

    public boolean possui(int necessario) { return quantidade >= necessario; }

    public void retirar(int qtd) {
        if (!possui(qtd)) {
            throw new OperacaoInvalidaException("Saldo insuficiente de " + sku);
        }
        quantidade -= qtd;
    }

    public void devolver(int qtd) { quantidade += qtd; }

    public void definirQuantidade(int nova) {
        if (nova < 0) throw new OperacaoInvalidaException("Quantidade não pode ser negativa");
        quantidade = nova;
    }

    public String getSku() { return sku; }
    public String getDescricao() { return descricao; }
    public int getQuantidade() { return quantidade; }
}
