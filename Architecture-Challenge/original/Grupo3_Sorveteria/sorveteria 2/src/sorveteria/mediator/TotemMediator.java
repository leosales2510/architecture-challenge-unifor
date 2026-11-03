// ==============================================================================
// INICIO DO PADRAO: MEDIATOR (usa os outros 4 padroes)
// Papel desta classe: Mediador concreto: coordena tudo
// ==============================================================================
package sorveteria.mediator;

import java.util.ArrayList;
import java.util.List;

import sorveteria.abstractfactory.FabricaLinha;
import sorveteria.abstractfactory.FabricaTradicional;
import sorveteria.abstractfactory.FabricaZero;
import sorveteria.abstractfactory.Sorvete;
import sorveteria.command.AdicionarItemCommand;
import sorveteria.command.AplicarCupomCommand;
import sorveteria.command.Comando;
import sorveteria.command.ComandoPedido;
import sorveteria.command.HistoricoComandos;
import sorveteria.command.RemoverItemCommand;
import sorveteria.decorator.CaldaChocolate;
import sorveteria.decorator.Granulado;
import sorveteria.memento.Pedido;

/**
 * MEDIATOR CONCRETO. Coordena os paineis, escolhe a fabrica da linha
 * (Abstract Factory), monta as coberturas (Decorator), cria e executa
 * os comandos (Command) e guarda o historico para o desfazer (Memento).
 * Ele coordena; quem faz o trabalho sao o Pedido e os comandos.
 */
public class TotemMediator implements Mediator {

    private final List<Componente> componentes = new ArrayList<>();
    private final Pedido pedido = new Pedido();
    private final HistoricoComandos historico = new HistoricoComandos();
    private FabricaLinha fabrica = new FabricaTradicional(); // linha padrao

    private final PainelCardapio cardapio;
    private final PainelCarrinho carrinho;
    private final PainelTotal total;

    public TotemMediator(PainelCardapio cardapio, PainelCarrinho carrinho, PainelTotal total) {
        this.cardapio = registrar(cardapio);
        this.carrinho = registrar(carrinho);
        this.total = registrar(total);
    }

    private <T extends Componente> T registrar(T c) {
        c.setMediator(this);
        componentes.add(c);
        return c;
    }

    @Override public void notificar(Componente remetente, String evento) {
        if (evento.startsWith("linha:")) {
            definirLinha(evento.substring(6));
        } else if (evento.equals("adicionar")) {
            adicionarItemMontado();
        } else if (evento.startsWith("remover:")) {
            removerItem(Integer.parseInt(evento.substring(8)));
        } else if (evento.equals("desfazer")) {
            desfazer();
        } else if (evento.equals("cupom")) {
            executar(new AplicarCupomCommand(pedido, "SORVETE10"));
        } else if (evento.equals("finalizar")) {
            System.out.printf(">> Pedido finalizado. Total a pagar: R$ %.2f%n", pedido.getTotal());
        }
    }

    public void definirLinha(String linha) {
        this.fabrica = linha.equalsIgnoreCase("Zero") ? new FabricaZero() : new FabricaTradicional();
        System.out.println(">> Totem agora usa a " + fabrica.getClass().getSimpleName()
                + " (linha " + fabrica.getNomeDaLinha() + ")");
    }

    public void aplicarCupom() { notificar(cardapio, "cupom"); }

    private void adicionarItemMontado() {
        // --- [ABSTRACT FACTORY] inicio: a fabrica da linha escolhida cria o produto ---
        Sorvete item = fabrica.criarSorvete(cardapio.getSabor());
        // --- [ABSTRACT FACTORY] fim ---

        // --- [DECORATOR] inicio: cada cobertura embrulha o item anterior ---
        for (String cobertura : cardapio.getCoberturas()) {
            item = switch (cobertura) {
                case "calda" -> new CaldaChocolate(item);
                case "granulado" -> new Granulado(item);
                default -> item;
            };
        }
        // --- [DECORATOR] fim ---

        // --- [COMMAND + MEMENTO] inicio: a acao vira objeto e salva o backup ---
        executar(new AdicionarItemCommand(pedido, item));
        // --- [COMMAND + MEMENTO] fim ---
    }

    private void removerItem(int posicao) {
        List<Sorvete> itens = pedido.getItens();
        if (posicao < 1 || posicao > itens.size()) {
            System.out.println(">> Posicao invalida");
            return;
        }
        executar(new RemoverItemCommand(pedido, itens.get(posicao - 1)));
    }

    private void executar(ComandoPedido comando) {
        comando.executar();            // [COMMAND] o comando age (e salva o memento antes)
        historico.empilhar(comando);   // [COMMAND] o historico guarda para o desfazer
        atualizarPaineis();            // [MEDIATOR] so o mediador avisa os paineis
    }

    public void desfazer() {
        Comando comando = historico.desempilhar();
        if (comando == null) {
            System.out.println(">> Nada para desfazer");
            return;
        }
        comando.desfazer(); // [MEMENTO] restaura a foto guardada pelo comando
        System.out.println(">> Desfeito: " + comando.getNome());
        atualizarPaineis();
    }

    private void atualizarPaineis() {
        List<String> descricoes = new ArrayList<>();
        for (Sorvete s : pedido.getItens()) {
            descricoes.add(String.format("%s (R$ %.2f)", s.getDescricao(), s.getPreco()));
        }
        carrinho.atualizar(descricoes);
        total.atualizar(pedido.getSubtotal(), pedido.getDesconto(), pedido.getTotal());
    }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR (usa os outros 4 padroes)  (arquivo TotemMediator.java)
