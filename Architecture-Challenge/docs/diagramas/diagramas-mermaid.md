# Diagramas UML em Mermaid (editáveis)

Os blocos abaixo são renderizados pelo GitHub, VS Code (extensão Markdown Preview Mermaid) e
[mermaid.live](https://mermaid.live). Para levá-los ao Draw.io: **Arranjar → Inserir → Avançado → Mermaid** e colar o bloco.

Arquivos `.drawio` desta pasta: `arquitetura-microsservicos.drawio`, `casos-de-uso.drawio`,
`classes-padroes.drawio` (as 48 classes dos padrões nos 3 serviços) e `sequencia-checkout.drawio`. Os diagramas do projeto anterior estão preservados em `original/`.

## 1. Arquitetura (visão de componentes)

```mermaid
flowchart LR
    C([Cliente]) --> F["Front-end React + Vite :5173<br/>(proxy = gateway de dev)"]
    O([Operador]) --> F
    F -->|GET /api/catalogo<br/>POST /montagens| CAT["catalogo-service :8081<br/>Abstract Factory · Builder · Decorator"]
    F -->|/api/pedidos/**| PED["pedido-service :8084<br/>Facade · Command · Memento · Mediator"]
    F -.->|GET/PUT /api/estoque| EST["estoque-service :8082<br/>reservas tudo-ou-nada"]
    PED -->|POST /montagens| CAT
    PED -->|disponibilidade · reservas<br/>confirmação · liberação| EST
    PED -->|POST /api/pagamentos| PAG["pagamento-service :8083<br/>Factory Method · Adapter"]
    PAG -.-> LEG[[LegacyCardProcessor<br/>SDK simulado]]
    CAT --- DB1[(H2 catalogo)]
    PED --- DB2[(H2 pedido)]
    EST --- DB3[(H2 estoque)]
    PAG --- DB4[(H2 pagamento)]
```

## 2. Classes — catalogo-service (Abstract Factory + Builder + Decorator)

```mermaid
classDiagram
    direction LR
    class FabricaLinha {
        <<interface>>
        +getLinha() Linha
        +criarSorvete(sabor) Sorvete
        +criarCasquinha() Casquinha
    }
    class FabricaTradicional
    class FabricaZero
    class FabricasPorLinha { +para(linha) FabricaLinha }
    class Sorvete {
        <<interface>>
        +getDescricao() String
        +getPreco() BigDecimal
        +getCasquinha() Casquinha
        +getComponentes() List~String~
    }
    class Casquinha { <<interface>> }
    class SorveteBase { <<abstract>> }
    class SorveteTradicional
    class SorveteZero
    class CasquinhaTradicional
    class CasquinhaZero
    class CoberturaDecorator {
        <<abstract>>
        #sorvete: Sorvete
        #trechoDescricao()* String
    }
    class CaldaChocolate
    class Granulado
    class Chantilly
    class CastanhaCaramelizada
    class CoberturasDisponiveis { +aplicar(item, sorvete) Sorvete }
    class SorveteBuilder {
        +daLinha(FabricaLinha) SorveteBuilder
        +comSabor(ItemCatalogo) SorveteBuilder
        +comCobertura(ItemCatalogo) SorveteBuilder
        +construir() Sorvete
    }
    class MontagemService { +montar(req) MontagemResponse }
    class CatalogoItens { <<interface>> +buscar(sku) ItemCatalogo }

    FabricaLinha <|.. FabricaTradicional
    FabricaLinha <|.. FabricaZero
    FabricaTradicional ..> SorveteTradicional : «cria»
    FabricaTradicional ..> CasquinhaTradicional : «cria»
    FabricaZero ..> SorveteZero : «cria»
    FabricaZero ..> CasquinhaZero : «cria»
    FabricaTradicional --> CatalogoItens
    FabricaZero --> CatalogoItens
    Sorvete <|.. SorveteBase
    SorveteBase <|-- SorveteTradicional
    SorveteBase <|-- SorveteZero
    Casquinha <|.. CasquinhaTradicional
    Casquinha <|.. CasquinhaZero
    Sorvete <|.. CoberturaDecorator
    CoberturaDecorator o--> "1" Sorvete : embrulha
    CoberturaDecorator <|-- CaldaChocolate
    CoberturaDecorator <|-- Granulado
    CoberturaDecorator <|-- Chantilly
    CoberturaDecorator <|-- CastanhaCaramelizada
    CoberturasDisponiveis ..> CoberturaDecorator : «cria»
    SorveteBuilder --> FabricaLinha
    SorveteBuilder --> CoberturasDisponiveis
    MontagemService ..> SorveteBuilder : «cria por requisição»
    MontagemService --> FabricasPorLinha
```

## 3. Classes — pedido-service (Facade + Command + Memento + Mediator)

```mermaid
classDiagram
    direction LR
    class PedidoController
    class PedidoFacade {
        +adicionarItem(id, req)
        +removerItem(id, codigo)
        +aplicarCupom(id, codigo)
        +desfazer(id)
        +finalizar(id, req) CheckoutResponse
    }
    class Pedido {
        <<Originator + Receiver>>
        +adicionar(ItemPedido)
        +remover(codigo)
        +aplicarCupom(CupomAplicado)
        +criarSnapshot() SnapshotPedido
        +restaurar(SnapshotPedido)
        +getTotal() BigDecimal
        +iniciarCheckout()
    }
    class SnapshotPedido {
        <<Memento>>
        ~itens()
        ~cupom()
    }
    class Comando {
        <<interface>>
        +executar(Pedido)
        +desfazer(Pedido)
        +getNome() String
    }
    class ComandoPedido {
        <<abstract>>
        -backup: SnapshotPedido
        #aplicar(Pedido)*
    }
    class AdicionarItemCommand
    class RemoverItemCommand
    class AplicarCupomCommand
    class HistoricoComandos {
        <<Caretaker>>
        +empilhar(Comando)
        +desempilhar() Optional~Comando~
    }
    class HistoricosPorPedido
    class CheckoutMediator {
        <<interface>>
        +finalizar(ctx)
        +notificar(remetente, evento, ctx)
    }
    class TotemCheckoutMediator
    class Participante {
        <<abstract>>
        #mediator: CheckoutMediator
    }
    class ParticipanteEstoque
    class ParticipantePagamento
    class ParticipantePedido
    class CatalogoClient { <<interface>> }
    class EstoqueClient { <<interface>> }
    class PagamentoClient { <<interface>> }

    PedidoController --> PedidoFacade
    PedidoFacade --> HistoricosPorPedido
    PedidoFacade --> CheckoutMediator
    PedidoFacade --> CatalogoClient
    PedidoFacade --> EstoqueClient
    PedidoFacade ..> Comando : cria e executa
    HistoricosPorPedido --> "*" HistoricoComandos
    HistoricoComandos o--> "*" Comando
    Comando <|.. ComandoPedido
    ComandoPedido <|-- AdicionarItemCommand
    ComandoPedido <|-- RemoverItemCommand
    ComandoPedido <|-- AplicarCupomCommand
    ComandoPedido --> SnapshotPedido : guarda (opaco)
    ComandoPedido ..> Pedido : receptor
    Pedido ..> SnapshotPedido : cria / restaura
    CheckoutMediator <|.. TotemCheckoutMediator
    Participante <|-- ParticipanteEstoque
    Participante <|-- ParticipantePagamento
    Participante <|-- ParticipantePedido
    Participante --> CheckoutMediator : notificar()
    TotemCheckoutMediator --> ParticipanteEstoque
    TotemCheckoutMediator --> ParticipantePagamento
    TotemCheckoutMediator --> ParticipantePedido
    ParticipanteEstoque --> EstoqueClient
    ParticipantePagamento --> PagamentoClient
```

## 4. Classes — pagamento-service (Factory Method + Adapter)

```mermaid
classDiagram
    direction LR
    class ProcessadorPagamento {
        <<abstract — Creator>>
        +metodo()* MetodoPagamento
        #criarGateway()* GatewayPagamento
        #validar(s)*
        +processar(s) ResultadoGateway
    }
    class ProcessadorCartao
    class ProcessadorPix
    class GatewayPagamento {
        <<interface — Target>>
        +cobrar(s) ResultadoGateway
    }
    class CartaoGatewayAdapter { <<Adapter>> }
    class PixGatewaySimulado
    class LegacyCardProcessor {
        <<Adaptee>>
        +authorize(pan, amountInCents, currency) LegacyAuthResponse
    }
    class PagamentoService

    ProcessadorPagamento <|-- ProcessadorCartao
    ProcessadorPagamento <|-- ProcessadorPix
    ProcessadorPagamento ..> GatewayPagamento : usa o produto
    ProcessadorCartao ..> CartaoGatewayAdapter : «cria»
    ProcessadorPix ..> PixGatewaySimulado : «cria»
    GatewayPagamento <|.. CartaoGatewayAdapter
    GatewayPagamento <|.. PixGatewaySimulado
    CartaoGatewayAdapter --> LegacyCardProcessor : traduz chamadas
    PagamentoService --> "*" ProcessadorPagamento : Map por método
```

## 5. Sequência — adicionar item e desfazer (Builder, Decorator, Command, Memento)

```mermaid
sequenceDiagram
    actor Cliente
    participant FE as Front-end
    participant PF as PedidoFacade
    participant CAT as catalogo-service
    participant EST as estoque-service
    participant CMD as AdicionarItemCommand
    participant P as Pedido
    participant H as HistoricoComandos

    Cliente->>FE: escolhe Zero, morango, calda, granulado
    FE->>CAT: POST /api/catalogo/montagens (prévia)
    CAT-->>FE: descrição + R$ 17,50
    Cliente->>FE: Adicionar ao pedido
    FE->>PF: POST /api/pedidos/{id}/itens
    PF->>CAT: POST /api/catalogo/montagens
    Note over CAT: SorveteBuilder → FabricaZero.criarSorvete()<br/>→ new Granulado(new CaldaChocolate(sorvete))
    CAT-->>PF: Montagem {precoUnitario, componentes}
    PF->>EST: POST /api/estoque/disponibilidade
    EST-->>PF: {disponivel: true}
    PF->>CMD: new AdicionarItemCommand(item)
    PF->>CMD: executar(pedido)
    CMD->>P: criarSnapshot()
    P-->>CMD: SnapshotPedido (backup)
    CMD->>P: adicionar(item)
    PF->>H: empilhar(comando)
    PF-->>FE: PedidoResponse (total, historicoDesfazer)
    Cliente->>FE: Desfazer
    FE->>PF: POST /api/pedidos/{id}/desfazer
    PF->>H: desempilhar()
    H-->>PF: comando
    PF->>CMD: desfazer(pedido)
    CMD->>P: restaurar(backup)
    PF-->>FE: PedidoResponse (estado anterior)
```

## 6. Estados do Pedido

```mermaid
stateDiagram-v2
    [*] --> ABERTO
    ABERTO --> ABERTO: adicionar / remover / cupom / desfazer
    ABERTO --> AGUARDANDO_PAGAMENTO: finalizar
    ABERTO --> CANCELADO: cancelar
    AGUARDANDO_PAGAMENTO --> PAGO: pagamento aprovado
    AGUARDANDO_PAGAMENTO --> PAGAMENTO_RECUSADO: pagamento recusado (reserva liberada)
    AGUARDANDO_PAGAMENTO --> ABERTO: falta de estoque / serviço indisponível
    AGUARDANDO_PAGAMENTO --> PAGAMENTO_EM_VERIFICACAO: resultado do pagamento incerto (reserva mantida)
    PAGAMENTO_EM_VERIFICACAO --> PAGO: reconciliação encontra aprovação
    PAGAMENTO_EM_VERIFICACAO --> PAGAMENTO_EM_VERIFICACAO: reconciliação sem aprovação (decisão manual)
    PAGAMENTO_RECUSADO --> AGUARDANDO_PAGAMENTO: nova tentativa
    PAGAMENTO_RECUSADO --> PAGAMENTO_RECUSADO: editar carrinho
    PAGAMENTO_RECUSADO --> CANCELADO: cancelar
    PAGO --> [*]
    CANCELADO --> [*]
```
