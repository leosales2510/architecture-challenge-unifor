package br.unifor.sorveteria.pagamento.domain;

/** Resposta normalizada de qualquer gateway (é isso que o Adapter entrega ao resto do sistema). */
public record ResultadoGateway(boolean aprovado, String codigoAutorizacao, String motivo) {
    public static ResultadoGateway aprovado(String codigoAutorizacao) {
        return new ResultadoGateway(true, codigoAutorizacao, null);
    }

    public static ResultadoGateway recusado(String motivo) {
        return new ResultadoGateway(false, null, motivo);
    }
}
