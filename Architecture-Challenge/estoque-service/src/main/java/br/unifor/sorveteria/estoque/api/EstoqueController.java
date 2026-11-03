package br.unifor.sorveteria.estoque.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unifor.sorveteria.estoque.api.dto.AjusteEstoqueRequest;
import br.unifor.sorveteria.estoque.api.dto.ConsultaDisponibilidadeRequest;
import br.unifor.sorveteria.estoque.api.dto.DisponibilidadeResponse;
import br.unifor.sorveteria.estoque.api.dto.ItemEstoqueResponse;
import br.unifor.sorveteria.estoque.api.dto.ReservaRequest;
import br.unifor.sorveteria.estoque.api.dto.ReservaResponse;
import br.unifor.sorveteria.estoque.domain.Falta;
import br.unifor.sorveteria.estoque.service.EstoqueService;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {
    private final EstoqueService service;

    public EstoqueController(EstoqueService service) { this.service = service; }

    @GetMapping
    public List<ItemEstoqueResponse> listar() {
        return service.listar().stream().map(ItemEstoqueResponse::de).toList();
    }

    @GetMapping("/{sku}")
    public ItemEstoqueResponse buscar(@PathVariable String sku) { return ItemEstoqueResponse.de(service.buscar(sku)); }

    /** Reposição / ajuste manual feito pelo operador da loja. */
    @PutMapping("/{sku}")
    public ItemEstoqueResponse ajustar(@PathVariable String sku, @Valid @RequestBody AjusteEstoqueRequest request) {
        return ItemEstoqueResponse.de(service.ajustar(sku, request.quantidade()));
    }

    @PostMapping("/disponibilidade")
    public DisponibilidadeResponse disponibilidade(@Valid @RequestBody ConsultaDisponibilidadeRequest request) {
        List<Falta> faltas = service.faltas(request.itens());
        return new DisponibilidadeResponse(faltas.isEmpty(), faltas);
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse reserva = ReservaResponse.de(service.reservar(request.pedidoId(), request.itens()));
        return ResponseEntity.created(URI.create("/api/estoque/reservas/" + reserva.id())).body(reserva);
    }

    @GetMapping("/reservas/{id}")
    public ReservaResponse reserva(@PathVariable Long id) { return ReservaResponse.de(service.buscarReserva(id)); }

    @PostMapping("/reservas/{id}/confirmacao")
    public ReservaResponse confirmar(@PathVariable Long id) { return ReservaResponse.de(service.confirmar(id)); }

    @DeleteMapping("/reservas/{id}")
    public ReservaResponse liberar(@PathVariable Long id) { return ReservaResponse.de(service.liberar(id)); }
}
