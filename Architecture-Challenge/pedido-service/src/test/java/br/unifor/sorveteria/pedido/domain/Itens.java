package br.unifor.sorveteria.pedido.domain;

import java.math.BigDecimal;
import java.util.List;

/** Itens prontos para os testes (mesmos preços do projeto original). */
public final class Itens {
    private Itens() { }

    /** Zero morango + calda + granulado = R$ 17,50. */
    public static ItemPedido morangoZeroComCaldaEGranulado() {
        return ItemPedido.novo("Sorvete zero açúcar de morango na casquinha integral sem açúcar + calda de chocolate + granulado",
                "ZERO_ACUCAR", "SAB-ZERO-MOR", List.of("COB-CALDA", "COB-GRAN"), 1, new BigDecimal("17.50"),
                List.of(new Componente("SAB-ZERO-MOR", 1), new Componente("CASQ-ZERO", 1),
                        new Componente("COB-CALDA", 1), new Componente("COB-GRAN", 1)));
    }

    /** Zero limão sem cobertura = R$ 13,50. */
    public static ItemPedido limaoZero(int quantidade) {
        return ItemPedido.novo("Sorvete zero açúcar de limão na casquinha integral sem açúcar", "ZERO_ACUCAR",
                "SAB-ZERO-LIM", List.of(), quantidade, new BigDecimal("13.50"),
                List.of(new Componente("SAB-ZERO-LIM", 1), new Componente("CASQ-ZERO", 1)));
    }

    public static CupomAplicado sorvete10() {
        return new CupomAplicado("SORVETE10", "10%", TipoDesconto.PERCENTUAL, new BigDecimal("10"), BigDecimal.ZERO);
    }

    public static CupomAplicado zero5() {
        return new CupomAplicado("ZERO5", "R$ 5", TipoDesconto.VALOR_FIXO, new BigDecimal("5.00"), new BigDecimal("20.00"));
    }
}
