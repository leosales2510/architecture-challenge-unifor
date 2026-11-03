package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Regra única de arredondamento monetário do serviço: 2 casas, HALF_UP (padrão comercial). */
public final class Dinheiro {
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private Dinheiro() { }

    public static BigDecimal arredondar(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
}
