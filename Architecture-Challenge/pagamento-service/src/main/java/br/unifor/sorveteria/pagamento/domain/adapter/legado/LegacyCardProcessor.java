// PADRÃO: ADAPTER — papel: ADAPTEE.
// Simula o SDK de uma adquirente de cartão que NÃO podemos alterar: API em inglês,
// valores em centavos (long), moeda ISO 4217 e respostas por códigos numéricos.
package br.unifor.sorveteria.pagamento.domain.adapter.legado;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

/**
 * Regras da simulação (cartões de teste, inspirados nos das adquirentes reais):
 * <ul>
 *   <li>final 0002 → "51" (saldo insuficiente)</li>
 *   <li>final 0005 → "05" (não autorizado)</li>
 *   <li>final 9999 → lança {@link LegacyTimeoutException} (provedor fora do ar)</li>
 *   <li>valor acima de 50.000 centavos → "61" (limite excedido)</li>
 *   <li>demais → "00" (aprovado)</li>
 * </ul>
 */
@Component
public class LegacyCardProcessor {

    public LegacyAuthResponse authorize(String pan, long amountInCents, String currencyIso4217) {
        if (!"BRL".equals(currencyIso4217)) return new LegacyAuthResponse("91", null);
        if (pan.endsWith("9999")) throw new LegacyTimeoutException("acquirer timeout after 30000ms");
        if (pan.endsWith("0002")) return new LegacyAuthResponse("51", null);
        if (pan.endsWith("0005")) return new LegacyAuthResponse("05", null);
        if (amountInCents > 50_000) return new LegacyAuthResponse("61", null);
        return new LegacyAuthResponse("00", String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000)));
    }

    public record LegacyAuthResponse(String responseCode, String authCode) { }

    public static class LegacyTimeoutException extends RuntimeException {
        public LegacyTimeoutException(String message) { super(message); }
    }
}
