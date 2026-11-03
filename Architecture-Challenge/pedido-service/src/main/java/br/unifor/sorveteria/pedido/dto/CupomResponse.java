package br.unifor.sorveteria.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unifor.sorveteria.pedido.domain.Cupom;
import br.unifor.sorveteria.pedido.domain.TipoDesconto;

public record CupomResponse(String codigo, String descricao, TipoDesconto tipo, BigDecimal valor, BigDecimal valorMinimo,
                            LocalDate validoAte) {
    public static CupomResponse de(Cupom c) {
        return new CupomResponse(c.getCodigo(), c.getDescricao(), c.getTipo(), c.getValor(), c.getValorMinimo(), c.getValidoAte());
    }
}
