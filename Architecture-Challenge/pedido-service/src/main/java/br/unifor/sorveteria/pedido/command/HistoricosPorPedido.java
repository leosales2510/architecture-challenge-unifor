package br.unifor.sorveteria.pedido.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Um HistoricoComandos por pedido, em memória. Decisão consciente (ver ADR-005):
 * o desfazer só vale enquanto o cliente está no totem; se o serviço reiniciar, o
 * pedido continua salvo no banco, mas o histórico de desfazer recomeça vazio.
 */
@Component
public class HistoricosPorPedido {
    private final Map<Long, HistoricoComandos> historicos = new ConcurrentHashMap<>();

    public HistoricoComandos de(Long pedidoId) {
        return historicos.computeIfAbsent(pedidoId, id -> new HistoricoComandos());
    }

    public void descartar(Long pedidoId) { historicos.remove(pedidoId); }
}
