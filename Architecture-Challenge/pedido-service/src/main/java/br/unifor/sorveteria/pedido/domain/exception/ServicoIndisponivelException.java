package br.unifor.sorveteria.pedido.domain.exception;

/** Outro microsserviço não respondeu (fora do ar, timeout ou erro 5xx). */
public class ServicoIndisponivelException extends RuntimeException {
    private final String servico;

    public ServicoIndisponivelException(String servico, Throwable causa) {
        super("Serviço de " + servico + " indisponível no momento", causa);
        this.servico = servico;
    }

    public String getServico() { return servico; }
}
