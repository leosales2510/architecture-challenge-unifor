# Totem da Sorveteria — Grupo 3 (Design Patterns GoF)

Implementação em Java dos 5 padrões do Grupo 3 no cenário do totem de
autoatendimento da sorveteria: **Abstract Factory, Decorator, Command,
Mediator e Memento**.

## Como compilar e executar

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out sorveteria.Main
```

Java 17 ou superior (o código usa `switch` com seta). Sem bibliotecas externas.

## Estrutura

```
src/sorveteria/
├── Main.java                         simulação do totem (roteiro do diagrama de sequência)
├── abstractfactory/
│   ├── FabricaLinha.java             fábrica abstrata (2 produtos)
│   ├── FabricaTradicional.java       fábrica concreta
│   ├── FabricaZero.java              fábrica concreta
│   ├── Sorvete.java                  produto abstrato / componente do Decorator
│   ├── SorveteTradicional.java       produto concreto
│   ├── SorveteZero.java              produto concreto
│   ├── Casquinha.java                produto abstrato
│   ├── CasquinhaTradicional.java     produto concreto
│   └── CasquinhaZero.java            produto concreto
├── decorator/
│   ├── CoberturaDecorator.java       decorator base (implementa Sorvete e guarda um Sorvete)
│   ├── CaldaChocolate.java           decorator concreto (+R$ 2,50)
│   └── Granulado.java                decorator concreto (+R$ 1,50)
├── command/
│   ├── Comando.java                  interface do Command
│   ├── ComandoPedido.java            base abstrata: guarda o backup (Memento)
│   ├── AdicionarItemCommand.java     comando concreto
│   ├── RemoverItemCommand.java       comando concreto
│   ├── AplicarCupomCommand.java      comando concreto
│   └── HistoricoComandos.java        caretaker: pilha de comandos
├── memento/
│   ├── Pedido.java                   originator (Memento) + receptor (Command)
│   └── SnapshotPedido.java           memento (getters package-private)
└── mediator/
    ├── Mediator.java                 interface do Mediator
    ├── TotemMediator.java            mediador concreto
    ├── Componente.java               colega abstrato
    ├── PainelCardapio.java           colega
    ├── PainelCarrinho.java           colega
    └── PainelTotal.java              colega
```

## Onde cada padrão está

| Padrão | Classes | Como verificar no código |
|---|---|---|
| Abstract Factory | `FabricaLinha`, `FabricaTradicional`, `FabricaZero` | A fábrica cria **dois** produtos (`criarSorvete` e `criarCasquinha`); com um só seria Factory Method. Nenhum `new SorveteZero(...)` aparece fora da fábrica. |
| Decorator | `CoberturaDecorator`, `CaldaChocolate`, `Granulado` | O decorator implementa `Sorvete` e guarda um `Sorvete`, então dá para empilhar: `new Granulado(new CaldaChocolate(sorvete))`. |
| Command | `Comando`, `ComandoPedido` e os 3 comandos | O comando não executa a regra: chama `pedido.adicionar(...)`, `pedido.remover(...)`, `pedido.aplicarCupom(...)`. |
| Mediator | `Mediator`, `TotemMediator`, `Componente` e os 3 painéis | Nenhum painel tem referência a outro painel. Todos só chamam `mediator.notificar(this, evento)`. |
| Memento | `Pedido`, `SnapshotPedido` | Os getters do snapshot são package-private: só o `Pedido` lê o conteúdo. O histórico guarda o comando, mas nunca olha dentro do memento. |

## A conexão Command + Memento

É o ponto forte da apresentação e está em `ComandoPedido`:

```java
protected void salvarBackup() { this.backup = pedido.criarSnapshot(); }

public void desfazer() {
    if (backup != null) pedido.restaurar(backup);
}
```

Cada comando chama `salvarBackup()` antes de mudar o pedido. O
`HistoricoComandos` guarda os comandos numa pilha; o desfazer desempilha o
último e manda ele restaurar o próprio backup.

## Saída esperada (resumo)

A `Main` executa 9 etapas: escolher a linha Zero, montar um sorvete com calda e
granulado, adicionar um segundo item, aplicar cupom, desfazer o cupom, desfazer
o item, remover um item, desfazer a remoção e finalizar. A cada ação o mediador
manda o carrinho e o total se atualizarem.

## Sobre as sugestões do outro grupo

- **Singleton:** não foi implementado. O Singleton é padrão do Grupo 1, e o
  cenário não tem o problema que ele resolve. As fábricas são objetos leves e
  sem estado, e o totem já cria um único `TotemMediator` no `Main`. Se o
  professor pedir, dá para transformar as fábricas em singletons sem mexer no
  resto do código.
- **Conferir padrões x diagrama de sequência:** o código segue a mesma ordem do
  diagrama — notificar → criar item → criar comando → snapshot → executar →
  empilhar → atualizar painéis.
- **Backup no diagrama de sequência:** no código, quem pede o snapshot é o
  comando (dentro de `executar()`), e não o mediador. Vale conferir se o
  diagrama mostra exatamente isso antes da entrega.
