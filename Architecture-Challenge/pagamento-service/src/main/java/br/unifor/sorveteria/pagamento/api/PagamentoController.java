package br.unifor.sorveteria.pagamento.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.unifor.sorveteria.pagamento.api.dto.PagamentoRequest;
import br.unifor.sorveteria.pagamento.api.dto.PagamentoResponse;
import br.unifor.sorveteria.pagamento.service.PagamentoService;
import br.unifor.sorveteria.pagamento.service.ResultadoProcessamento;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {
    private final PagamentoService service;

    public PagamentoController(PagamentoService service) { this.service = service; }

    /**
     * 201 com status APROVADO ou RECUSADO (recusa é resultado de negócio, não erro HTTP);
     * 200 quando o pedido já tinha pagamento aprovado (chamada repetida, idempotente).
     */
    @PostMapping
    public ResponseEntity<PagamentoResponse> pagar(@Valid @RequestBody PagamentoRequest request) {
        ResultadoProcessamento resultado = service.processar(request.paraSolicitacao());
        PagamentoResponse corpo = PagamentoResponse.de(resultado.pagamento());
        return resultado.novo()
                ? ResponseEntity.created(URI.create("/api/pagamentos/" + corpo.id())).body(corpo)
                : ResponseEntity.ok(corpo);
    }

    @GetMapping("/{id}")
    public PagamentoResponse buscar(@PathVariable Long id) { return PagamentoResponse.de(service.buscar(id)); }

    @GetMapping
    public List<PagamentoResponse> doPedido(@RequestParam Long pedidoId) {
        return service.doPedido(pedidoId).stream().map(PagamentoResponse::de).toList();
    }
}
