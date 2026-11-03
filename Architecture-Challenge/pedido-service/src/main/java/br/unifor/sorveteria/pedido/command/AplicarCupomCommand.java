// PADRÃO: COMMAND — papel: comando concreto.
package br.unifor.sorveteria.pedido.command;

import br.unifor.sorveteria.pedido.domain.CupomAplicado;
import br.unifor.sorveteria.pedido.domain.Pedido;

public class AplicarCupomCommand extends ComandoPedido {
    private final CupomAplicado cupom;

    public AplicarCupomCommand(CupomAplicado cupom) { this.cupom = cupom; }

    @Override protected void aplicar(Pedido receptor) { receptor.aplicarCupom(cupom); }

    @Override public String getNome() { return "Aplicar cupom " + cupom.getCodigo(); }
}
