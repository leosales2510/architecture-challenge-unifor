package br.unifor.sorveteria.pedido.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.unifor.sorveteria.pedido.domain.CupomAplicado;
import br.unifor.sorveteria.pedido.domain.ItemPedido;
import br.unifor.sorveteria.pedido.domain.Pedido;
import br.unifor.sorveteria.pedido.domain.StatusPedido;

public record PedidoResponse(Long id, StatusPedido status, boolean editavel, List<Item> itens,
                             BigDecimal subtotal, BigDecimal desconto, BigDecimal total, Cupom cupom,
                             List<String> historicoDesfazer, String mensagem, Long reservaId, Long pagamentoId,
                             String codigoAutorizacao, Instant criadoEm, Instant atualizadoEm) {

    public record Item(String codigo, String descricao, String linha, String saborSku, List<String> coberturas,
                       int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        static Item de(ItemPedido i) {
            return new Item(i.getCodigo(), i.getDescricao(), i.getLinha(), i.getSaborSku(), i.getCoberturas(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public record Cupom(String codigo, String descricao, boolean ativo) {
        static Cupom de(CupomAplicado c, BigDecimal subtotal) {
            return c == null ? null : new Cupom(c.getCodigo(), c.getDescricao(), c.atendeMinimo(subtotal));
        }
    }

    /** @param historicoDesfazer nomes dos comandos que podem ser desfeitos, do mais recente ao mais antigo */
    public static PedidoResponse de(Pedido p, List<String> historicoDesfazer) {
        return new PedidoResponse(p.getId(), p.getStatus(), p.isEditavel(), p.getItens().stream().map(Item::de).toList(),
                p.getSubtotal(), p.getDesconto(), p.getTotal(), Cupom.de(p.getCupom(), p.getSubtotal()),
                p.isEditavel() ? historicoDesfazer : List.of(), p.getMensagem(), p.getReservaId(), p.getPagamentoId(),
                p.getCodigoAutorizacao(), p.getCriadoEm(), p.getAtualizadoEm());
    }
}
