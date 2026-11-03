package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;

/**
 * Polimorfismo (GRASP) / Aberto-Fechado: cada tipo de cupom sabe calcular o seu
 * desconto. Um novo tipo entra como nova constante, sem if/else espalhado.
 * (No projeto original só existia "SORVETE10" fixo dentro de Pedido.aplicarCupom.)
 */
public enum TipoDesconto {
    PERCENTUAL {
        @Override public BigDecimal calcular(BigDecimal subtotal, BigDecimal valor) {
            return subtotal.multiply(valor).divide(BigDecimal.valueOf(100));
        }
    },
    VALOR_FIXO {
        @Override public BigDecimal calcular(BigDecimal subtotal, BigDecimal valor) {
            return valor;
        }
    };

    public abstract BigDecimal calcular(BigDecimal subtotal, BigDecimal valor);
}
