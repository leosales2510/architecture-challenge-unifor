package br.unifor.sorveteria.catalogo.domain;

/** As duas famílias de produtos do totem (cada uma tem a sua fábrica concreta). */
public enum Linha {
    TRADICIONAL("Tradicional"),
    ZERO_ACUCAR("Zero Açúcar");

    private final String nome;

    Linha(String nome) { this.nome = nome; }

    public String getNome() { return nome; }
}
