package br.unifor.sorveteria.catalogo.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unifor.sorveteria.catalogo.api.dto.CatalogoResponse;
import br.unifor.sorveteria.catalogo.api.dto.ItemResponse;
import br.unifor.sorveteria.catalogo.api.dto.MontagemRequest;
import br.unifor.sorveteria.catalogo.api.dto.MontagemResponse;
import br.unifor.sorveteria.catalogo.service.CatalogoService;
import br.unifor.sorveteria.catalogo.service.MontagemService;

/** Controller (GRASP): recebe o HTTP e delega; nenhuma regra de negócio aqui. */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
    private final CatalogoService catalogoService;
    private final MontagemService montagemService;

    public CatalogoController(CatalogoService catalogoService, MontagemService montagemService) {
        this.catalogoService = catalogoService;
        this.montagemService = montagemService;
    }

    @GetMapping
    public CatalogoResponse cardapio() { return catalogoService.cardapio(); }

    @GetMapping("/itens/{sku}")
    public ItemResponse item(@PathVariable String sku) { return catalogoService.item(sku); }

    /** Calcula descrição, preço e componentes de um sorvete. Não grava nada (é uma simulação de montagem). */
    @PostMapping("/montagens")
    public MontagemResponse montar(@Valid @RequestBody MontagemRequest request) {
        return montagemService.montar(request);
    }
}
