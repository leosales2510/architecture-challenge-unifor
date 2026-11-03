package br.unifor.sorveteria.catalogo.config;

import static br.unifor.sorveteria.catalogo.domain.Linha.TRADICIONAL;
import static br.unifor.sorveteria.catalogo.domain.Linha.ZERO_ACUCAR;
import static br.unifor.sorveteria.catalogo.domain.TipoItem.BASE;
import static br.unifor.sorveteria.catalogo.domain.TipoItem.CASQUINHA;
import static br.unifor.sorveteria.catalogo.domain.TipoItem.COBERTURA;
import static br.unifor.sorveteria.catalogo.domain.TipoItem.SABOR;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.unifor.sorveteria.catalogo.domain.ItemCatalogo;
import br.unifor.sorveteria.catalogo.repository.ItemCatalogoRepository;

/**
 * Cardápio inicial. Os preços de base, casquinha, calda e granulado são os mesmos
 * do projeto original do Grupo 3 (ex.: Zero morango + calda + granulado = R$ 17,50).
 * Os SKUs são os mesmos cadastrados no estoque-service.
 */
@Component
class CargaInicialCatalogo implements ApplicationRunner {
    private final ItemCatalogoRepository repository;

    CargaInicialCatalogo(ItemCatalogoRepository repository) { this.repository = repository; }

    @Override public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        repository.saveAll(List.of(
                new ItemCatalogo("BASE-TRAD", "Sorvete tradicional", BASE, TRADICIONAL, preco("9.00")),
                new ItemCatalogo("BASE-ZERO", "Sorvete zero açúcar", BASE, ZERO_ACUCAR, preco("11.00")),
                new ItemCatalogo("CASQ-TRAD", "casquinha tradicional", CASQUINHA, TRADICIONAL, preco("2.00")),
                new ItemCatalogo("CASQ-ZERO", "casquinha integral sem açúcar", CASQUINHA, ZERO_ACUCAR, preco("2.50")),
                new ItemCatalogo("SAB-TRAD-CHOC", "Chocolate", SABOR, TRADICIONAL, BigDecimal.ZERO),
                new ItemCatalogo("SAB-TRAD-MOR", "Morango", SABOR, TRADICIONAL, BigDecimal.ZERO),
                new ItemCatalogo("SAB-TRAD-CRE", "Creme", SABOR, TRADICIONAL, BigDecimal.ZERO),
                new ItemCatalogo("SAB-TRAD-FLO", "Flocos", SABOR, TRADICIONAL, BigDecimal.ZERO),
                new ItemCatalogo("SAB-ZERO-MOR", "Morango", SABOR, ZERO_ACUCAR, BigDecimal.ZERO),
                new ItemCatalogo("SAB-ZERO-LIM", "Limão", SABOR, ZERO_ACUCAR, BigDecimal.ZERO),
                new ItemCatalogo("SAB-ZERO-CHOC", "Chocolate 70%", SABOR, ZERO_ACUCAR, BigDecimal.ZERO),
                new ItemCatalogo("COB-CALDA", "Calda de chocolate", COBERTURA, null, preco("2.50")),
                new ItemCatalogo("COB-GRAN", "Granulado", COBERTURA, null, preco("1.50")),
                new ItemCatalogo("COB-CHANT", "Chantilly", COBERTURA, null, preco("2.00")),
                new ItemCatalogo("COB-CAST", "Castanha caramelizada", COBERTURA, null, preco("3.00"))));
    }

    private static BigDecimal preco(String valor) { return new BigDecimal(valor); }
}
