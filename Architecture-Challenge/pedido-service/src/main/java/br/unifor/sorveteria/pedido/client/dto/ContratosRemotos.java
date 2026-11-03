package br.unifor.sorveteria.pedido.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contratos explícitos dos outros serviços, do ponto de vista do pedido-service.
 * São cópias intencionais (não há biblioteca compartilhada — ver ADR-003): cada
 * serviço evolui o seu modelo interno sem quebrar os demais, desde que o JSON
 * destes campos seja mantido. Campos extras recebidos são ignorados.
 */
public final class ContratosRemotos {
    private ContratosRemotos() { }

    // ---- catalogo-service: POST /api/catalogo/montagens
    public record MontagemRequest(String linha, String saborSku, List<String> coberturas) { }

    public record Montagem(String linha, String saborSku, List<String> coberturas, String descricao,
                           BigDecimal precoUnitario, List<ItemQuantidade> componentes) { }

    // ---- estoque-service: /api/estoque/disponibilidade e /api/estoque/reservas
    public record ItemQuantidade(String sku, int quantidade) { }

    public record FaltaRemota(String sku, int solicitado, int disponivel) { }

    public record ConsultaDisponibilidade(List<ItemQuantidade> itens) { }

    public record Disponibilidade(boolean disponivel, List<FaltaRemota> faltas) { }

    public record ReservaRequest(Long pedidoId, List<ItemQuantidade> itens) { }

    public record Reserva(Long id, Long pedidoId, String status) { }

    // ---- pagamento-service: POST /api/pagamentos
    public record PagamentoRequest(Long pedidoId, BigDecimal valor, String metodo, String numeroCartao) { }

    public record Pagamento(Long id, Long pedidoId, BigDecimal valor, String metodo, String status,
                            String codigoAutorizacao, String motivoRecusa) {
        public boolean aprovado() { return "APROVADO".equals(status); }
    }

    // ---- corpo de erro (RFC 7807) comum aos serviços
    public record Problema(String codigo, String detail, List<FaltaRemota> faltas) { }
}
