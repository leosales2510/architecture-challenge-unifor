package br.unifor.sorveteria.pedido.api;

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

import br.unifor.sorveteria.pedido.dto.CheckoutResponse;
import br.unifor.sorveteria.pedido.dto.CupomRequest;
import br.unifor.sorveteria.pedido.dto.FinalizacaoRequest;
import br.unifor.sorveteria.pedido.dto.NovoItemRequest;
import br.unifor.sorveteria.pedido.dto.PedidoResponse;
import br.unifor.sorveteria.pedido.facade.PedidoFacade;

/** Controller (GRASP): traduz HTTP <-> casos de uso. Depende apenas da Facade. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoFacade facade;

    public PedidoController(PedidoFacade facade) { this.facade = facade; }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar() {
        PedidoResponse pedido = facade.criar();
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.id())).body(pedido);
    }

    @GetMapping("/{id}")
    public PedidoResponse consultar(@PathVariable Long id) { return facade.consultar(id); }

    @PostMapping("/{id}/itens")
    public PedidoResponse adicionarItem(@PathVariable Long id, @Valid @RequestBody NovoItemRequest request) {
        return facade.adicionarItem(id, request);
    }

    @DeleteMapping("/{id}/itens/{codigoItem}")
    public PedidoResponse removerItem(@PathVariable Long id, @PathVariable String codigoItem) {
        return facade.removerItem(id, codigoItem);
    }

    @PutMapping("/{id}/cupom")
    public PedidoResponse aplicarCupom(@PathVariable Long id, @Valid @RequestBody CupomRequest request) {
        return facade.aplicarCupom(id, request.codigo());
    }

    @PostMapping("/{id}/desfazer")
    public PedidoResponse desfazer(@PathVariable Long id) { return facade.desfazer(id); }

    @GetMapping("/{id}/historico")
    public List<String> historico(@PathVariable Long id) { return facade.historico(id); }

    @PostMapping("/{id}/finalizacao")
    public CheckoutResponse finalizar(@PathVariable Long id, @Valid @RequestBody FinalizacaoRequest request) {
        return facade.finalizar(id, request);
    }

    /** Reconcilia um pedido em PAGAMENTO_EM_VERIFICACAO consultando o pagamento-service. */
    @PostMapping("/{id}/reconciliacao")
    public PedidoResponse reconciliar(@PathVariable Long id) { return facade.reconciliarPagamento(id); }

    @PostMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable Long id) { return facade.cancelar(id); }
}
