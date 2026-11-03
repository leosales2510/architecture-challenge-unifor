# 6. APIs REST

Erros seguem RFC 7807 com campo `codigo` (tabela em `02-arquitetura.md` §2.7).
Exemplos executáveis em `docs/api/requests.http`; o roteiro completo por `curl` está em `scripts/smoke-test.sh`.

## catalogo-service — `http://localhost:8081`

| Método | Caminho | Descrição | Respostas |
|---|---|---|---|
| GET | `/api/catalogo` | Cardápio: linhas (preço base, casquinha, sabores), coberturas, `maxCoberturas` | 200 |
| GET | `/api/catalogo/itens/{sku}` | Um item do catálogo | 200, 404 |
| POST | `/api/catalogo/montagens` | Monta um sorvete (Abstract Factory + Builder + Decorator). Não grava nada | 200, 400, 404, 422 |

```http
POST /api/catalogo/montagens
{"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"]}

200 OK
{"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"],
 "descricao":"Sorvete zero açúcar de morango na casquinha integral sem açúcar + calda de chocolate + granulado",
 "precoUnitario":17.50,
 "componentes":[{"sku":"SAB-ZERO-MOR","quantidade":1},{"sku":"CASQ-ZERO","quantidade":1},
                {"sku":"COB-CALDA","quantidade":1},{"sku":"COB-GRAN","quantidade":1}]}
```

## pedido-service — `http://localhost:8084`

| Método | Caminho | Corpo | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/api/pedidos` | — | Abre um pedido (`ABERTO`) | 201 |
| GET | `/api/pedidos/{id}` | — | Consulta | 200, 404 |
| POST | `/api/pedidos/{id}/itens` | `{linha, saborSku, coberturas[], quantidade}` | Adiciona item (preço vem do catálogo; estoque consultado) | 200, 400, 404, 409, 422, 503 |
| DELETE | `/api/pedidos/{id}/itens/{codigo}` | — | Remove item | 200, 404, 409 |
| PUT | `/api/pedidos/{id}/cupom` | `{codigo}` | Aplica/troca cupom | 200, 409, 422 |
| POST | `/api/pedidos/{id}/desfazer` | — | Desfaz a última ação (Command + Memento) | 200, 409 |
| GET | `/api/pedidos/{id}/historico` | — | Pilha de ações desfazíveis (topo primeiro) | 200 |
| POST | `/api/pedidos/{id}/finalizacao` | `{metodo: CARTAO\|PIX, numeroCartao?}` | Checkout (Mediator) | 200, 400, 402, 409, 422, 502, 503 |
| POST | `/api/pedidos/{id}/reconciliacao` | — | Pedido em `PAGAMENTO_EM_VERIFICACAO`: conclui como `PAGO` só se o pagamento-service mostrar um pagamento aprovado | 200, 404, 409, 503 |
| POST | `/api/pedidos/{id}/cancelamento` | — | Cancela pedido editável | 200, 409 |
| GET | `/api/cupons` | — | Cupons ativos | 200 |

`PedidoResponse`: `id, status, editavel, itens[{codigo, descricao, linha, saborSku, coberturas, quantidade,
precoUnitario, subtotal}], subtotal, desconto, total, cupom{codigo, descricao, ativo}, historicoDesfazer[],
mensagem, reservaId, pagamentoId, codigoAutorizacao, criadoEm, atualizadoEm`.

Checkout aprovado (200):
```json
{"resultado":"APROVADO","mensagem":"Pagamento aprovado",
 "trilha":["CHECKOUT_INICIADO","ESTOQUE_RESERVADO","PAGAMENTO_APROVADO","ESTOQUE_CONFIRMADO","PEDIDO_ATUALIZADO"],
 "faltas":[],"pedido":{"id":3,"status":"PAGO","codigoAutorizacao":"483920", "...":"..."}}
```
Checkout recusado (402, `application/problem+json`):
```json
{"status":402,"codigo":"PAGAMENTO_RECUSADO","detail":"Saldo insuficiente",
 "trilha":["CHECKOUT_INICIADO","ESTOQUE_RESERVADO","PAGAMENTO_RECUSADO","ESTOQUE_LIBERADO","PEDIDO_ATUALIZADO"],
 "pedido":{"status":"PAGAMENTO_RECUSADO","editavel":true,"...":"..."}}
```

**Resultado incerto (502 `PAGAMENTO_EM_VERIFICACAO`)**: a resposta do pagamento se perdeu ou veio ilegível. O
pedido fica em `PAGAMENTO_EM_VERIFICACAO` (não editável, não cancelável, não cobrável de novo) e a reserva é
mantida. `POST /reconciliacao` sem pagamento aprovado registrado responde 409 `PAGAMENTO_NAO_CONFIRMADO`
e não altera nada.

Cupons cadastrados: `SORVETE10` (10%, do projeto original), `ZERO5` (R$ 5,00 a partir de R$ 20,00),
`VERAO25` (expirado ontem — para demonstrar a validação).

## estoque-service — `http://localhost:8082`

| Método | Caminho | Corpo | Descrição | Respostas |
|---|---|---|---|---|
| GET | `/api/estoque` | — | Saldos | 200 |
| GET | `/api/estoque/{sku}` | — | Saldo de um SKU | 200, 404 |
| PUT | `/api/estoque/{sku}` | `{quantidade}` | Ajuste/reposição pelo operador | 200, 400, 404 |
| POST | `/api/estoque/disponibilidade` | `{itens[{sku, quantidade}]}` | Consulta sem alterar saldo | 200 |
| POST | `/api/estoque/reservas` | `{pedidoId, itens[]}` | Reserva tudo-ou-nada (baixa o saldo) | 201, 400, 409 |
| GET | `/api/estoque/reservas/{id}` | — | Consulta reserva | 200, 404 |
| POST | `/api/estoque/reservas/{id}/confirmacao` | — | Confirma (pagamento aprovado) | 200, 422 |
| DELETE | `/api/estoque/reservas/{id}` | — | Libera e devolve saldo (idempotente) | 200, 422 |

Saldos iniciais baixos de propósito: `SAB-ZERO-LIM` = 3, `COB-CAST` = 2.

## pagamento-service — `http://localhost:8083`

| Método | Caminho | Corpo | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/api/pagamentos` | `{pedidoId, valor, metodo, numeroCartao?}` | Cobra. Recusa = 201 com `status: RECUSADO`. Pedido já pago = 200 com o pagamento existente (também sob chamadas simultâneas) | 200, 201, 400, 422, 503 |
| GET | `/api/pagamentos/{id}` | — | Consulta | 200, 404 |
| GET | `/api/pagamentos?pedidoId=` | — | Tentativas de um pedido | 200 |

Cartões de teste do simulador: final **1111** aprova · **0002** saldo insuficiente · **0005** não autorizado ·
**9999** operadora fora do ar (503) · valor > R$ 500 recusa por limite. Pix aprova até R$ 1.000.
