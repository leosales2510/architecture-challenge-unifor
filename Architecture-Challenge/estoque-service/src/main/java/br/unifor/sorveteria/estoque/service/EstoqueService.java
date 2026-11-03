package br.unifor.sorveteria.estoque.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unifor.sorveteria.estoque.api.dto.ItemQuantidade;
import br.unifor.sorveteria.estoque.domain.Falta;
import br.unifor.sorveteria.estoque.domain.ItemEstoque;
import br.unifor.sorveteria.estoque.domain.ItemReservado;
import br.unifor.sorveteria.estoque.domain.Reserva;
import br.unifor.sorveteria.estoque.domain.StatusReserva;
import br.unifor.sorveteria.estoque.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.estoque.domain.exception.RecursoNaoEncontradoException;
import br.unifor.sorveteria.estoque.repository.ItemEstoqueRepository;
import br.unifor.sorveteria.estoque.repository.ReservaRepository;

/**
 * Regras do estoque. A reserva é "tudo ou nada": ou todos os SKUs têm saldo e
 * todos são baixados na mesma transação, ou nada muda e a resposta lista as faltas.
 */
@Service
@Transactional
public class EstoqueService {
    private final ItemEstoqueRepository itens;
    private final ReservaRepository reservas;

    public EstoqueService(ItemEstoqueRepository itens, ReservaRepository reservas) {
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<ItemEstoque> listar() { return itens.findAllByOrderBySku(); }

    @Transactional(readOnly = true)
    public ItemEstoque buscar(String sku) {
        return itens.findById(sku).orElseThrow(() -> new RecursoNaoEncontradoException("SKU não cadastrado no estoque: " + sku));
    }

    public ItemEstoque ajustar(String sku, int quantidade) {
        ItemEstoque item = buscar(sku);
        item.definirQuantidade(quantidade);
        return item;
    }

    @Transactional(readOnly = true)
    public List<Falta> faltas(List<ItemQuantidade> pedido) {
        List<Falta> faltas = new ArrayList<>();
        somarPorSku(pedido).forEach((sku, necessario) -> {
            int disponivel = itens.findById(sku).map(ItemEstoque::getQuantidade).orElse(0);
            if (disponivel < necessario) faltas.add(new Falta(sku, necessario, disponivel));
        });
        return faltas;
    }

    public Reserva reservar(Long pedidoId, List<ItemQuantidade> pedido) {
        // Uma nova tentativa de checkout do mesmo pedido substitui a reserva pendente anterior.
        reservas.findByPedidoIdAndStatus(pedidoId, StatusReserva.PENDENTE).forEach(this::devolverSaldo);

        List<Falta> faltas = faltas(pedido);
        if (!faltas.isEmpty()) throw new EstoqueInsuficienteException(faltas);

        Map<String, Integer> porSku = somarPorSku(pedido);
        List<ItemReservado> reservados = new ArrayList<>();
        porSku.forEach((sku, qtd) -> {
            buscar(sku).retirar(qtd);
            reservados.add(new ItemReservado(sku, qtd));
        });
        return reservas.save(new Reserva(pedidoId, reservados));
    }

    public Reserva confirmar(Long reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        reserva.confirmar();
        return reserva;
    }

    /** Compensação: devolve ao saldo o que a reserva tinha retirado. Idempotente. */
    public Reserva liberar(Long reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        devolverSaldo(reserva);
        return reserva;
    }

    @Transactional(readOnly = true)
    public Reserva buscarReserva(Long id) {
        return reservas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada: " + id));
    }

    private void devolverSaldo(Reserva reserva) {
        if (reserva.liberar()) {
            reserva.getItens().forEach(i -> buscar(i.getSku()).devolver(i.getQuantidade()));
        }
    }

    private static Map<String, Integer> somarPorSku(List<ItemQuantidade> pedido) {
        Map<String, Integer> porSku = new LinkedHashMap<>();
        pedido.forEach(i -> porSku.merge(i.sku(), i.quantidade(), Integer::sum));
        return porSku;
    }
}
