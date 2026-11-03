package br.unifor.sorveteria.estoque.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import br.unifor.sorveteria.estoque.domain.exception.OperacaoInvalidaException;

/** Reserva feita por um pedido durante o checkout. As transições de estado ficam aqui (Information Expert). */
@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status = StatusReserva.PENDENTE;

    @Column(nullable = false)
    private Instant criadaEm = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_itens", joinColumns = @JoinColumn(name = "reserva_id"))
    private List<ItemReservado> itens = new ArrayList<>();

    protected Reserva() { }

    public Reserva(Long pedidoId, List<ItemReservado> itens) {
        this.pedidoId = pedidoId;
        this.itens = new ArrayList<>(itens);
    }

    public void confirmar() {
        if (status == StatusReserva.LIBERADA) {
            throw new OperacaoInvalidaException("Reserva " + id + " já foi liberada e não pode ser confirmada");
        }
        status = StatusReserva.CONFIRMADA;
    }

    /** @return true se a liberação aconteceu agora (e o saldo deve ser devolvido); false se já estava liberada. */
    public boolean liberar() {
        if (status == StatusReserva.CONFIRMADA) {
            throw new OperacaoInvalidaException("Reserva " + id + " já foi confirmada e não pode ser liberada");
        }
        if (status == StatusReserva.LIBERADA) return false;
        status = StatusReserva.LIBERADA;
        return true;
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public StatusReserva getStatus() { return status; }
    public Instant getCriadaEm() { return criadaEm; }
    public List<ItemReservado> getItens() { return List.copyOf(itens); }
}
