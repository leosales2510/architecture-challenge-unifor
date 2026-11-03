// ==============================================================================
// INICIO DO PADRAO: MEDIATOR
// Papel desta classe: Colega concreto
// ==============================================================================
package sorveteria.mediator;

import java.util.Arrays;
import java.util.List;

/** Onde o cliente escolhe linha, sabor e coberturas. */
public class PainelCardapio extends Componente {
    private String sabor;
    private List<String> coberturas = List.of();

    public void escolherLinha(String linha) {
        System.out.println("[Cardapio] cliente escolheu a linha " + linha);
        mediator.notificar(this, "linha:" + linha);
    }

    public void selecionar(String sabor, String... coberturas) {
        this.sabor = sabor;
        this.coberturas = Arrays.asList(coberturas);
        System.out.println("[Cardapio] cliente montou: " + sabor + " " + this.coberturas);
        mediator.notificar(this, "adicionar");
    }

    public String getSabor() { return sabor; }
    public List<String> getCoberturas() { return coberturas; }
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: MEDIATOR  (arquivo PainelCardapio.java)
