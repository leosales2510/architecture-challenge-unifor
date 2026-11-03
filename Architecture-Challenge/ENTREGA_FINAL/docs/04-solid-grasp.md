# 4. SOLID e GRASP — onde estão no código

Só estão listados princípios com evidência concreta. Caminhos relativos a
`<servico>/src/main/java/br/unifor/sorveteria/<servico>/`.

## 4.1 SOLID

### S — Responsabilidade Única
| Classe | Única razão para mudar |
|---|---|
| `catalogo/domain/builder/SorveteBuilder` | regras de composição de um sorvete (antes estavam no `TotemMediator`) |
| `catalogo/domain/decorator/CoberturasDisponiveis` | mapeamento SKU → decorator |
| `pagamento/domain/adapter/CartaoGatewayAdapter` | tradução entre o nosso contrato e o SDK da operadora |
| `pedido/api/PedidoController` | protocolo HTTP (cada método delega em uma linha) |
| `pedido/api/ApiExceptionHandler` | mapeamento exceção → resposta RFC 7807 |
| `pedido/domain/Dinheiro` | política de arredondamento monetário |

O caso mais claro é a refatoração do `TotemMediator` original, que escolhia fábrica, montava coberturas,
criava comandos e imprimia painéis: essas tarefas foram para `FabricasPorLinha`, `SorveteBuilder`,
`PedidoFacade` e o front-end, respectivamente.

### O — Aberto/Fechado
- **Nova linha de sorvete:** criar `FabricaXyz implements FabricaLinha` com `@Component`;
  `FabricasPorLinha` a registra pelo construtor `FabricasPorLinha(List<FabricaLinha>)` sem edição.
- **Novo meio de pagamento:** criar `ProcessadorXyz extends ProcessadorPagamento`; o
  `PagamentoService(List<ProcessadorPagamento>, ...)` o registra no `EnumMap` sem `if/switch` por meio.
- **Novo tipo de cupom:** nova constante em `pedido/domain/TipoDesconto` com seu `calcular(...)`.

### L — Substituição de Liskov
- Qualquer `CoberturaDecorator` substitui um `Sorvete`: os testes empilham decorators e chamam
  `getPreco()`, `getDescricao()`, `getComponentes()` sem saber o tipo concreto.
- `CartaoGatewayAdapter` e `PixGatewaySimulado` respeitam o mesmo contrato de `GatewayPagamento`:
  recusa é retorno (`ResultadoGateway.recusado`), indisponibilidade é `GatewayIndisponivelException` —
  nenhum dos dois muda essa semântica.
- Os `*HttpClient` e os mocks dos testes são intercambiáveis atrás de `CatalogoClient`/`EstoqueClient`/`PagamentoClient`
  (é o que permite `PedidoApiIntegrationTest` usar `@MockitoBean`).

### I — Segregação de Interfaces
- Os clientes remotos do pedido-service são três interfaces pequenas, uma por serviço, em vez de um
  "ClienteDeServicos" geral. `ParticipantePagamento` só depende de `PagamentoClient` (1 método).
- `catalogo/domain/CatalogoItens` expõe só `buscar(sku)` — o que as fábricas precisam — e não o
  `JpaRepository` inteiro.

### D — Inversão de Dependência
- `FabricaTradicional`/`FabricaZero` dependem da abstração `CatalogoItens`; a implementação JPA
  (`catalogo/repository/CatalogoItensJpa`) depende do domínio, não o contrário. Nos testes, a mesma
  fábrica roda com `CatalogoEmMemoria`.
- `PedidoFacade` depende de `CatalogoClient`, `EstoqueClient` e `CheckoutMediator` (interfaces);
  as implementações HTTP ficam em `pedido/client/*HttpClient`.
- `PedidoFacade` recebe um `java.time.Clock` (validade dos cupons testável).

## 4.2 GRASP

| Princípio | Evidência |
|---|---|
| **Information Expert** | `Pedido.getSubtotal()/getDesconto()/getTotal()/componentesNecessarios()`; `ItemPedido.getSubtotal()`; `CupomAplicado.descontoSobre()`; `Cupom.aplicavelEm(data)`; `ItemCatalogo.pertenceA(linha)`; `ItemEstoque.possui/retirar`; `Reserva.confirmar/liberar` (cada objeto decide sobre os próprios dados). |
| **Creator** | `FabricaZero` cria `SorveteZero`/`CasquinhaZero` (tem os dados de inicialização); `Pedido` cria `SnapshotPedido` (contém os dados); `ItemPedido.novo(...)` nasce dos dados do catálogo; `Pagamento.registrar(solicitacao, resultado)`. |
| **Controller** | `PedidoController`, `CatalogoController`, `EstoqueController`, `PagamentoController` recebem os eventos do sistema (requisições) e delegam; nenhum contém regra de negócio. No checkout, `TotemCheckoutMediator` é o controlador do caso de uso. |
| **Low Coupling** | Serviços se conhecem só por contratos JSON (`ContratosRemotos`), sem biblioteca compartilhada; o estoque só conhece SKUs; participantes do checkout não se conhecem; o controller só conhece a fachada. |
| **High Cohesion** | Pacotes por responsabilidade (`abstractfactory/`, `decorator/`, `builder/`, `command/`, `checkout/`, `client/`); cada serviço trata um único assunto do domínio. |
| **Polymorphism** | `TipoDesconto.PERCENTUAL/VALOR_FIXO.calcular(...)`; `ProcessadorPagamento.criarGateway()`; `CoberturaDecorator.trechoDescricao()`; `SorveteBase.prefixo()` — variação por tipo sem condicionais. |
| **Pure Fabrication** | `HistoricosPorPedido`, `FabricasPorLinha`, `CoberturasDisponiveis`, `Dinheiro`: não existem no domínio da sorveteria, foram inventadas para manter coesão e baixo acoplamento. |
| **Indirection** | Portas `*Client` entre a fachada e o HTTP; `CheckoutMediator` entre os participantes. |
| **Protected Variations** | `CartaoGatewayAdapter` protege o sistema de mudanças no SDK da operadora; `ContratosRemotos` ignora campos novos dos outros serviços. |
