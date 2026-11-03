# 2. Arquitetura

## 2.1 Visão geral

O totem virou um sistema distribuído com **4 microsserviços Spring Boot** e um **front-end React**.
Cada serviço é um processo separado, com `pom.xml`, porta e banco de dados próprios, e pode ser
compilado, testado e executado sozinho (`cd catalogo-service && mvn spring-boot:run`).

| Serviço | Porta | Responsabilidade (dono dos dados de…) | Padrões GoF | Chama |
|---|---|---|---|---|
| `catalogo-service` | 8081 | linhas, sabores, casquinhas, coberturas, **preços** e montagem do produto | Abstract Factory, Builder, Decorator | — |
| `estoque-service` | 8082 | **saldos por SKU** e reservas (pendente → confirmada / liberada) | — | — |
| `pagamento-service` | 8083 | **tentativas de cobrança** (cartão/Pix simulados) | Factory Method, Adapter | SDK simulado (em processo) |
| `pedido-service` | 8084 | **pedidos**, itens, cupons, histórico de desfazer, checkout | Facade, Command, Memento, Mediator | catálogo, estoque, pagamento |
| `frontend` | 5173 | interface do totem + painel do operador | — | catálogo, pedido, estoque (via proxy) |

Diagramas: `docs/diagramas/arquitetura-microsservicos.drawio` e seção 1 de `docs/diagramas/diagramas-mermaid.md`.

## 2.2 Por que microsserviços — e por que esta divisão

O enunciado exige microsserviços; o trabalho da equipe é mostrar que a divisão **não é um monólito
cortado em quatro portas**. O critério usado foi: *cada serviço é dono de um conjunto de dados que muda
por motivos diferentes* (alta coesão interna, baixo acoplamento entre serviços).

- **Catálogo** muda quando a loja muda o cardápio ou os preços. Nada aqui depende de pedidos.
- **Estoque** muda a cada venda e a cada reposição. Ele conhece só SKU + quantidade: não sabe preço nem
  descrição comercial. O SKU é o único identificador compartilhado com o catálogo.
- **Pagamentos** muda quando se troca de operadora ou se adiciona um meio de pagamento. É o único que
  conhece o SDK da operadora; o número completo do cartão nunca sai dele (guarda só os 4 últimos dígitos).
- **Pedidos** é o núcleo do caso de uso "comprar": guarda uma *cópia* dos dados do catálogo no momento da
  compra (descrição, preço, componentes), para que uma mudança de preço não altere pedidos existentes.

### Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Juntar estoque ao catálogo | Leituras do cardápio (muitas, raras mudanças) e baixas de estoque (concorrentes, a cada venda) têm perfis diferentes; o estoque precisa de bloqueio otimista, o catálogo não. |
| Serviço separado de "cupons/promoções" | Hoje há 3 cupons e uma regra simples; um quinto serviço aumentaria a latência do carrinho sem ganho real. Fica dentro do pedido-service, isolado em `Cupom`/`TipoDesconto` para ser extraído no futuro se crescer. |
| Serviço de usuários/autenticação | O totem é anônimo (o cliente não faz login). Sem caso de uso que justifique. |
| Mensageria (Kafka/RabbitMQ) | O fluxo do totem é interativo: o cliente espera a resposta na tela. REST síncrono com compensação resolve o problema com muito menos infraestrutura (ADR-006). |

## 2.3 Arquitetura interna: Service Layer (camadas)

Todos os serviços seguem a mesma organização, escolhida em vez de MVC porque **não há views no
back-end** — a "View" é o front-end React, em outro processo. O que cada serviço expõe é uma API, e a
Service Layer separa melhor o protocolo HTTP das regras de negócio.

```
api/          Controllers REST + ApiExceptionHandler (HTTP <-> casos de uso; sem regra de negócio)
dto/ ou api/dto/   Records de entrada/saída (validação Jakarta: @NotBlank, @Min, @Pattern...)
service/ ou facade/   Casos de uso; coordenam domínio, repositórios e clientes
domain/       Entidades e padrões GoF (o "coração": regras, totais, transições de estado)
repository/   Spring Data JPA (padrão Repository)
client/       (pedido-service) portas e adaptadores HTTP para os outros serviços
config/       Beans de infraestrutura e carga inicial
```

Regra de dependência: `api → service/facade → domain ← repository/client`. O domínio do pedido não
importa nada de HTTP; os clientes HTTP implementam interfaces (`CatalogoClient`, `EstoqueClient`,
`PagamentoClient`) que a fachada usa — isso é o que permite testar a fachada e o mediador com mocks.

## 2.4 Comunicação entre serviços

- **REST síncrono** com `RestClient` (Spring 6.1+), um bean por serviço em `pedido-service/.../config/ClientesHttpConfig.java`.
- **Timeouts** de 2 s (conexão) e 5 s (leitura): o totem não pode travar esperando um serviço caído.
- **Contratos explícitos**: o pedido-service declara exatamente o JSON que envia e recebe em
  `client/dto/ContratosRemotos.java`. Campos extras recebidos são ignorados (tolerância a evolução).
  Os testes `ContratosHttpTest` verificam o corpo enviado e a interpretação de cada resposta.
- **URLs configuráveis** por variável de ambiente: `CATALOGO_URL`, `ESTOQUE_URL`, `PAGAMENTO_URL`.
- **Front-end → serviços** pelo proxy do Vite (`frontend/vite.config.js`): `/api/catalogo` → 8081,
  `/api/estoque` → 8082, `/api/pagamentos` → 8083, `/api/pedidos` e `/api/cupons` → 8084.

## 2.5 Persistência e propriedade dos dados

Cada serviço tem seu **próprio banco H2** (`jdbc:h2:mem:catalogo`, `...:estoque`, `...:pagamento`,
`...:pedido`). Nenhum serviço acessa a tabela de outro: o pedido só sabe o saldo do estoque
perguntando à API do estoque. Console H2 de cada serviço: `http://localhost:808X/h2-console`
(usuário `sa`, sem senha, JDBC URL acima) — útil para mostrar ao professor os quatro bancos separados.

## 2.6 Consistência no checkout (saga orquestrada)

Não há transação distribuída. O `TotemCheckoutMediator` executa uma **saga orquestrada**:

1. Pedido → `AGUARDANDO_PAGAMENTO` (gravado; a `@Version` impede um segundo clique simultâneo).
2. Estoque: **reserva tudo-ou-nada** (o saldo é baixado já na reserva, status `PENDENTE`).
3. Pagamento: cobrança.
4. Aprovado → confirma a reserva e marca o pedido `PAGO`.
   Recusado → **libera a reserva (compensação)** e marca `PAGAMENTO_RECUSADO` (o cliente pode tentar outro meio).
   Falta de estoque ou serviço fora do ar → libera o que tiver sido reservado e devolve o pedido para `ABERTO`.

Proteções adicionais:
- **Idempotência no pagamento**: se o pedido já tem pagamento aprovado, uma nova chamada devolve o
  mesmo pagamento (HTTP 200) em vez de cobrar de novo. Vale também para chamadas **simultâneas**: a
  verificação, a cobrança e a gravação ocorrem sob uma trava por pedido, solta só após o commit
  (`PagamentoService.processar`). Limitação: a trava é da instância — com várias instâncias seria
  preciso uma trava no banco (backlog B28).
- **Classificação do resultado financeiro** (`PagamentoHttpClient` + `SituacaoPagamento`): a decisão de
  liberar o estoque depende do que se sabe sobre o dinheiro.

  | Situação | Como é identificada | O que acontece |
  |---|---|---|
  | Comprovadamente não cobrado | erro antes da cobrança; 4xx; 503 `GATEWAY_INDISPONIVEL`; conexão recusada | libera a reserva, pedido `ABERTO` |
  | Recusado | `status: RECUSADO` | libera a reserva, pedido `PAGAMENTO_RECUSADO` (editável) |
  | Aprovado | `status: APROVADO` | confirma a reserva, pedido `PAGO` — nunca libera |
  | **Incerto** | timeout de leitura, 5xx desconhecido, resposta ilegível, exceção com a cobrança em andamento | **mantém a reserva**, pedido `PAGAMENTO_EM_VERIFICACAO`, sem nova cobrança nem cancelamento |

  Reconciliação (`POST /api/pedidos/{id}/reconciliacao`): conclui como `PAGO` somente se o
  pagamento-service mostrar um pagamento aprovado; a ausência de aprovação **não** é tratada como
  "não cobrado" (a resposta pode estar atrasada) — o pedido segue bloqueado para decisão manual (B29).
  Na simulação, o 503 `GATEWAY_INDISPONIVEL` significa que o SDK falhou antes de autorizar; com uma
  operadora real, esse caso também exigiria consulta à operadora.
- **Reserva substitui reserva**: uma nova tentativa de checkout do mesmo pedido libera a reserva
  pendente anterior antes de criar outra.
- **Bloqueio otimista** (`@Version`) em `Pedido` e `ItemEstoque`.
- **Erro inesperado no meio do checkout**: `PedidoFacade.recuperarCheckoutInterrompido` aplica a mesma
  tabela acima a partir da `SituacaoPagamento` registrada no contexto (recusado ⇒ libera e reabre;
  aprovado ⇒ conclui `PAGO`; incerto ⇒ em verificação), então o pedido nunca fica preso em
  `AGUARDANDO_PAGAMENTO` e o estoque nunca é liberado com o dinheiro em dúvida.

## 2.7 Tratamento de erros

Todos os serviços respondem erros no formato **RFC 7807** (`application/problem+json`) com um campo
estável `codigo`, que o cliente usa para decidir o que fazer (o texto `detail` é só para humanos):

| HTTP | `codigo` | Quando |
|---|---|---|
| 400 | `DADOS_INVALIDOS`, `CORPO_INVALIDO` | Bean Validation falhou / JSON malformado ou enum desconhecido (inclui `erros` por campo) |
| 402 | `PAGAMENTO_RECUSADO` | Checkout com cobrança recusada (inclui `trilha` e `pedido`) |
| 404 | `NAO_ENCONTRADO`, `ITEM_NAO_ENCONTRADO` | Pedido, item, SKU ou reserva inexistente |
| 409 | `ESTOQUE_INSUFICIENTE` | Falta de saldo (inclui `faltas`: sku, solicitado, disponível) |
| 409 | `PEDIDO_NAO_EDITAVEL`, `NADA_PARA_DESFAZER`, `CONFLITO_CONCORRENCIA` | Regra de estado violada |
| 422 | `COMPOSICAO_INVALIDA`, `ITEM_INVALIDO`, `CUPOM_INVALIDO`, `DADOS_PAGAMENTO_INVALIDOS` | Regra de negócio |
| 409 | `PAGAMENTO_NAO_CONFIRMADO` | Reconciliação sem pagamento aprovado registrado (nada muda) |
| 502 | `PAGAMENTO_EM_VERIFICACAO` | Resultado da cobrança desconhecido; pedido bloqueado até reconciliar |
| 503 | `SERVICO_INDISPONIVEL`, `GATEWAY_INDISPONIVEL`, `FALHA_INTEGRACAO` | Outro serviço ou a operadora não respondeu (inclui `servico`) |
| 400 | `PARAMETRO_INVALIDO` | (pedido-service) parâmetro de caminho com formato inválido, ex.: `/api/pedidos/abc` |
| 500 | `ERRO_INTERNO` | (pedido-service) erro inesperado; o status de erros do próprio Spring (404 de rota, 405) é preservado |

## 2.8 Valores monetários

`BigDecimal` em todos os serviços, colunas `DECIMAL(10,2)`, arredondamento único **HALF_UP** com 2
casas (`pedido-service/.../domain/Dinheiro.java`). O desconto é calculado sobre o subtotal já
arredondado e nunca excede o subtotal. No adaptador de cartão, reais são convertidos para centavos com
`movePointRight(2).longValueExact()` (lança exceção em vez de truncar).
