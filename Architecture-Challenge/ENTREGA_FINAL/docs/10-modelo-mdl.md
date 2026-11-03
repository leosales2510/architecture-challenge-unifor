# 10. Diagrama de arquitetura no MDL (mdlmodel.com)

**Situação: o arquivo MDL NÃO foi gerado.** Não tive acesso à ferramenta nem conheço um formato de
importação confiável para ela. Este documento traz o modelo completo para a equipe desenhar
manualmente no mdlmodel.com (estimativa: 1–2 h, item B04 do backlog). Depois, exporte uma imagem e
coloque o link/print nos slides.

A fonte de verdade é o mesmo modelo de `docs/diagramas/arquitetura-microsservicos.drawio`.

## Elementos

| ID | Tipo sugerido | Nome | Descrição curta | Tecnologia |
|---|---|---|---|---|
| A1 | Ator/Pessoa | Cliente | Monta o sorvete e paga no totem | — |
| A2 | Ator/Pessoa | Operador da loja | Acompanha e repõe estoque | — |
| E1 | Sistema externo | Operadora de cartão (simulada) | SDK `LegacyCardProcessor` | Java (em processo) |
| F1 | Aplicação web | Front-end do Totem | Montador, carrinho, pagamento, painel de estoque | React 18 + Vite, :5173 |
| S1 | Serviço | catalogo-service | Cardápio, preços, montagem | Spring Boot 3.5, :8081 |
| S2 | Serviço | pedido-service | Carrinho, cupons, desfazer, checkout | Spring Boot 3.5, :8084 |
| S3 | Serviço | estoque-service | Saldos e reservas | Spring Boot 3.5, :8082 |
| S4 | Serviço | pagamento-service | Cobranças cartão/Pix | Spring Boot 3.5, :8083 |
| D1 | Banco de dados | catalogo DB | itens_catalogo | H2 |
| D2 | Banco de dados | pedido DB | pedidos, itens_pedido, cupons | H2 |
| D3 | Banco de dados | estoque DB | itens_estoque, reservas, reserva_itens | H2 |
| D4 | Banco de dados | pagamento DB | pagamentos | H2 |

## Relações (todas síncronas, HTTP/JSON)

| De | Para | Rótulo |
|---|---|---|
| A1 | F1 | usa |
| A2 | F1 | consulta / repõe estoque |
| F1 | S1 | GET /api/catalogo, POST /montagens |
| F1 | S2 | /api/pedidos/**, /api/cupons |
| F1 | S3 | GET/PUT /api/estoque |
| S2 | S1 | POST /api/catalogo/montagens |
| S2 | S3 | disponibilidade, reservas, confirmação, liberação |
| S2 | S4 | POST /api/pagamentos |
| S4 | E1 | authorize() via Adapter |
| S1 | D1 · S2 → D2 · S3 → D3 · S4 → D4 | lê/grava (JPA) |

## Agrupamentos sugeridos

- Limite "Totem da Sorveteria" contendo F1, S1–S4 e D1–D4.
- Anotação em cada serviço com os padrões: S1 = Abstract Factory, Builder, Decorator · S2 = Facade,
  Command, Memento, Mediator · S4 = Factory Method, Adapter.
- Nota: "um banco por serviço; nenhum serviço acessa o banco de outro".
