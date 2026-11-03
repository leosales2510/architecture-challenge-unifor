package br.unifor.sorveteria.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Dados de pagamento repassados ao pagamento-service (o número do cartão não é gravado neste serviço). */
public record FinalizacaoRequest(
        @NotBlank @Pattern(regexp = "CARTAO|PIX", message = "deve ser CARTAO ou PIX") String metodo,
        String numeroCartao) {
}
