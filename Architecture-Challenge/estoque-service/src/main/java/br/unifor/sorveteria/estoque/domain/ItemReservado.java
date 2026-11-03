package br.unifor.sorveteria.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemReservado {
    @Column(nullable = false, length = 40)
    private String sku;

    @Column(nullable = false)
    private int quantidade;

    protected ItemReservado() { }

    public ItemReservado(String sku, int quantidade) {
        this.sku = sku;
        this.quantidade = quantidade;
    }

    public String getSku() { return sku; }
    public int getQuantidade() { return quantidade; }
}
