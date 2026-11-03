package br.unifor.sorveteria.pagamento.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import br.unifor.sorveteria.pagamento.domain.MetodoPagamento;
import br.unifor.sorveteria.pagamento.domain.SolicitacaoPagamento;

public record PagamentoRequest(
        @NotNull Long pedidoId,
        @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "no máximo 2 casas decimais") BigDecimal valor,
        @NotNull MetodoPagamento metodo,
        String numeroCartao) {

    public SolicitacaoPagamento paraSolicitacao() {
        String cartao = numeroCartao == null || numeroCartao.isBlank() ? null : numeroCartao.replaceAll("[\\s-]", "");
        return new SolicitacaoPagamento(pedidoId, valor, metodo, cartao);
    }
}
