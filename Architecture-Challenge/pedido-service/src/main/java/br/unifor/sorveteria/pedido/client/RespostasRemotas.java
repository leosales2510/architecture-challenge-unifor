package br.unifor.sorveteria.pedido.client;

import org.springframework.web.client.RestClientResponseException;

import br.unifor.sorveteria.pedido.client.dto.ContratosRemotos.Problema;

/** Lê o corpo RFC 7807 devolvido por outro serviço, tolerando corpos inesperados. */
final class RespostasRemotas {
    private RespostasRemotas() { }

    static Problema problema(RestClientResponseException e) {
        try {
            Problema p = e.getResponseBodyAs(Problema.class);
            if (p != null) return p;
        } catch (RuntimeException ignorado) {
            // corpo não é JSON: cai no texto genérico abaixo
        }
        return new Problema(null, "HTTP " + e.getStatusCode().value(), null);
    }
}
