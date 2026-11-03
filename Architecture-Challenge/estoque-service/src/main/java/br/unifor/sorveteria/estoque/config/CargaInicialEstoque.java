package br.unifor.sorveteria.estoque.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.unifor.sorveteria.estoque.domain.ItemEstoque;
import br.unifor.sorveteria.estoque.repository.ItemEstoqueRepository;

/**
 * Saldos iniciais. Limão Zero (3) e Castanha (2) começam baixos de propósito,
 * para demonstrar o fluxo de "estoque insuficiente" na apresentação.
 */
@Component
class CargaInicialEstoque implements ApplicationRunner {
    private final ItemEstoqueRepository repository;

    CargaInicialEstoque(ItemEstoqueRepository repository) { this.repository = repository; }

    @Override public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        repository.saveAll(List.of(
                new ItemEstoque("CASQ-TRAD", "Casquinha tradicional", 30),
                new ItemEstoque("CASQ-ZERO", "Casquinha integral sem açúcar", 30),
                new ItemEstoque("SAB-TRAD-CHOC", "Sorvete de chocolate (porção)", 20),
                new ItemEstoque("SAB-TRAD-MOR", "Sorvete de morango (porção)", 20),
                new ItemEstoque("SAB-TRAD-CRE", "Sorvete de creme (porção)", 20),
                new ItemEstoque("SAB-TRAD-FLO", "Sorvete de flocos (porção)", 20),
                new ItemEstoque("SAB-ZERO-MOR", "Sorvete zero de morango (porção)", 15),
                new ItemEstoque("SAB-ZERO-LIM", "Sorvete zero de limão (porção)", 3),
                new ItemEstoque("SAB-ZERO-CHOC", "Sorvete zero de chocolate 70% (porção)", 15),
                new ItemEstoque("COB-CALDA", "Calda de chocolate (porção)", 25),
                new ItemEstoque("COB-GRAN", "Granulado (porção)", 25),
                new ItemEstoque("COB-CHANT", "Chantilly (porção)", 25),
                new ItemEstoque("COB-CAST", "Castanha caramelizada (porção)", 2)));
    }
}
