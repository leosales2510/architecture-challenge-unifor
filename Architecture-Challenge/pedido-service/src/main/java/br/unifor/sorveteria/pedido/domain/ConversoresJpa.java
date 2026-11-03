package br.unifor.sorveteria.pedido.domain;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Listas pequenas e imutáveis do item gravadas como texto ("SKU:1,SKU:2").
 * Evita tabelas filhas do item, o que permite carregar o pedido inteiro de uma vez.
 */
final class ConversoresJpa {
    private ConversoresJpa() { }

    @Converter
    static class ListaDeTexto implements AttributeConverter<List<String>, String> {
        @Override public String convertToDatabaseColumn(List<String> lista) {
            return lista == null ? "" : String.join(",", lista);
        }

        @Override public List<String> convertToEntityAttribute(String texto) {
            return texto == null || texto.isBlank() ? List.of() : List.of(texto.split(","));
        }
    }

    @Converter
    static class ListaDeComponentes implements AttributeConverter<List<Componente>, String> {
        @Override public String convertToDatabaseColumn(List<Componente> lista) {
            return lista == null ? "" : String.join(",", lista.stream().map(c -> c.sku() + ":" + c.quantidade()).toList());
        }

        @Override public List<Componente> convertToEntityAttribute(String texto) {
            if (texto == null || texto.isBlank()) return List.of();
            return Arrays.stream(texto.split(",")).map(par -> {
                String[] partes = par.split(":");
                return new Componente(partes[0], Integer.parseInt(partes[1]));
            }).toList();
        }
    }
}
