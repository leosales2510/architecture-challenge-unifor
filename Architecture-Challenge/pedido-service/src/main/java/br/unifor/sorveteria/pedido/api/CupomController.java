package br.unifor.sorveteria.pedido.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unifor.sorveteria.pedido.dto.CupomResponse;
import br.unifor.sorveteria.pedido.repository.CupomRepository;

@RestController
@RequestMapping("/api/cupons")
public class CupomController {
    private final CupomRepository cupons;

    public CupomController(CupomRepository cupons) { this.cupons = cupons; }

    @GetMapping
    public List<CupomResponse> listar() {
        return cupons.findByAtivoTrueOrderByCodigo().stream().map(CupomResponse::de).toList();
    }
}
