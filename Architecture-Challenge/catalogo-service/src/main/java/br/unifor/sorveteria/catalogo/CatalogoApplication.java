package br.unifor.sorveteria.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microsserviço de Catálogo: dono dos sabores, linhas, casquinhas, coberturas e preços. */
@SpringBootApplication
public class CatalogoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogoApplication.class, args);
    }
}
