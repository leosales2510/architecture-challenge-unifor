// PADRÃO: ABSTRACT FACTORY + DECORATOR (herdado do projeto do Grupo 3)
// Papel: produto abstrato da fábrica E componente que os decorators de cobertura embrulham.
package br.unifor.sorveteria.catalogo.domain.abstractfactory;

import java.math.BigDecimal;
import java.util.List;

import br.unifor.sorveteria.catalogo.domain.Linha;

public interface Sorvete {
    String getDescricao();

    /** Preço unitário em reais (BigDecimal, escala 2). */
    BigDecimal getPreco();

    Casquinha getCasquinha();

    Linha getLinha();

    /** SKUs consumidos do estoque por uma unidade deste produto (sabor, casquinha e coberturas). */
    List<String> getComponentes();
}
