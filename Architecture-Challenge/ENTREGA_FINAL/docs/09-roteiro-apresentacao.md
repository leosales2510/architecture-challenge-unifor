# 9. Roteiro da apresentação e da demonstração

Tempo sugerido: 15–20 min + perguntas. Entre colchetes, quem apresenta.

## 9.1 Slides

Deck final: `ENTREGA_FINAL/Apresentacao_Architecture_Challenge.key` (também `.pptx` e `.pdf`). A fala completa de
cada slide está em `ENTREGA_FINAL/Roteiro_5_Integrantes.md`, igual às notas do apresentador. [I1]…[I5] = Integrante 1…5
do roteiro (divisão da fala; a divisão do código está em `docs/08` §8.1).

1. **Capa** [I1]: Grupo 3, nomes dos integrantes.
2. **Mesmo domínio, arquitetura nova** [I1]: o fluxo do totem; do console Java com 5 padrões aos 4 microsserviços (`docs/01`).
3. **Funcionalidades e diagramas UML** [I1]: casos de uso, classes e sequência do checkout.
4. **Arquitetura geral** [I2]: diagrama; um banco por serviço; REST com contratos JSON.
5. **Os quatro microsserviços** [I2]: portas, responsabilidades e padrões de cada um (`docs/02` §2.1).
6. **Comunicação REST e o checkout distribuído** [I2]: reservar → cobrar → concluir; compensação; RFC 7807.
7. **Decisões arquiteturais (ADRs)** [I2]: `docs/05`.
8. **Padrões criacionais** [I3]: Abstract Factory, Builder, Factory Method; diferença AF × FM.
9. **Padrões estruturais** [I3]: Decorator, Adapter, Facade.
10. **Padrões comportamentais** [I3]: Command, Memento, Mediator.
11. **SOLID e GRASP no código** [I3]: `docs/04`.
12. **Demonstração ao vivo** [I4]: seção 9.2 (versão de 4 min no roteiro).
13. **Testes automatizados e resultados** [I5]: 85 testes JUnit 5, smoke test 22/22.
14. **Limitações conhecidas e próximos passos** [I5]: itens `Futuro` do backlog (B21–B25, B28, B29).
15. **Conclusão** [I5].

O print do quadro de backlog (B18) e a imagem do MDL (B04) ainda não existem. Quando estiverem prontos, podem
entrar como slide extra ou ser mostrados ao vivo, se o professor pedir.

## 9.2 Demonstração (≈ 6 min)

Antes: `./scripts/start-all.sh`, `cd frontend && npm run dev`, abrir `http://localhost:5173`, abrir o
"Painel do operador · Estoque" no rodapé.

| # | Ação no totem | O que dizer | Padrão |
|---|---|---|---|
| 1 | Linha **Zero Açúcar** | a casquinha e o preço base mudam juntos | Abstract Factory |
| 2 | Morango + calda + granulado | prévia: "…+ calda de chocolate + granulado", R$ 17,50 — mesmo valor do projeto original, calculado pelo catálogo | Decorator, Builder |
| 3 | Marcar as 4 coberturas | erro "Máximo de 3 coberturas" vindo do back-end (422) | Builder |
| 4 | Deixar só calda e granulado, **Adicionar**; depois Limão Zero sem cobertura | total R$ 31,00 (17,50 + 13,50, como no original) | Command |
| 5 | Cupom `SORVETE10`, depois `VERAO25` | 10% aplicado; VERAO25 recusado por expiração | Command, Polimorfismo |
| 6 | **Desfazer** duas vezes | botão mostra o nome do próximo comando; totais voltam | Memento |
| 7 | Limão Zero, quantidade 4 | 409 "estoque insuficiente" (saldo 3) | integração com estoque |
| 8 | Finalizar → **Cartão recusado** | 402, motivo "Saldo insuficiente", trilha com `ESTOQUE_LIBERADO`; painel de estoque mostra saldo devolvido | Mediator + compensação, Adapter |
| 9 | Finalizar → **Operadora fora do ar** | 503, pedido volta ao carrinho | Adapter (timeout → exceção do domínio) |
| 10 | Finalizar → **Pix** | aprovado `PIX-…`, saldo do estoque baixou | Factory Method |
| 11 (opcional) | Parar o estoque (`kill $(cat .pids/estoque-service.pid)`) e adicionar item | 503 `SERVICO_INDISPONIVEL`, os outros serviços continuam no ar | independência dos serviços |

Se o front-end falhar na hora: o mesmo roteiro existe em `docs/api/requests.http` e `scripts/smoke-test.sh`.

## 9.3 Perguntas prováveis e respostas fundamentadas no código

**1. "Isso não é um monólito dividido em quatro portas?"**
Não: cada serviço tem banco próprio e nenhum lê a tabela de outro. O pedido só sabe o saldo perguntando
à API do estoque (`EstoqueHttpClient.verificar`). O estoque nem conhece preços. Dá para derrubar o
estoque e o catálogo continua servindo o cardápio (demonstração 11).

**2. "Qual a diferença entre a Abstract Factory e o Factory Method de vocês?"**
`FabricaLinha` é um *objeto* que cria uma *família* de dois produtos (sorvete + casquinha). O Factory
Method é *um método* (`criarGateway()`) sobrescrito por *subclasses* de `ProcessadorPagamento`, criando
*um* produto.

**3. "Por que o Mediator saiu dos painéis?"**
Painéis de tela não existem no back-end; no React isso é estado de componente. O lugar onde várias peças
precisam reagir umas às outras é o checkout: em `TotemCheckoutMediator.notificar`, `PAGAMENTO_RECUSADO`
dispara `estoque.liberar` e `pedido.registrarRecusa`, sem que o participante de pagamento conheça os outros.

**4. "O Command recebe o Pedido como parâmetro. Isso ainda é Command?"**
Sim. O comando continua encapsulando a requisição (parâmetros + estado para desfazer) e o invocador
(`PedidoFacade`) continua desacoplado de como cada ação é desfeita. A mudança é só de quando o receptor
é ligado: em REST o pedido é recarregado do banco a cada requisição (comentário em `Comando.java`).

**5. "E se o pagamento for aprovado mas a resposta se perder?"**
O pedido-service não sabe se houve cobrança, então **não** libera a reserva nem permite nova cobrança:
o pedido vai para `PAGAMENTO_EM_VERIFICACAO` (`PagamentoHttpClient` classifica timeout de leitura e 5xx
como incertos). A reconciliação (`POST /reconciliacao`) consulta o pagamento-service e só conclui como
`PAGO` se encontrar a aprovação (teste `resultadoIncertoBloqueiaNovaCobranca...`). E mesmo que uma
cobrança fosse repetida, o pagamento-service devolveria a aprovação existente — inclusive para chamadas
simultâneas (`PagamentoConcorrenteTest`).

**6. "Dois clientes compram o último limão ao mesmo tempo?"**
A reserva é transacional e `ItemEstoque` tem `@Version`: a segunda transação falha com conflito
(o estoque responde 409 `CONFLITO_CONCORRENCIA`) e não deixa saldo negativo; o pedido-service trata isso
como falha do checkout, libera o que houver e devolve o pedido ao carrinho para nova tentativa. A verificação ao adicionar item é só uma
prévia; a reserva no checkout é a verificação definitiva.

**7. "Por que BigDecimal e HALF_UP?"**
`double` não representa 0,10 exatamente. `Dinheiro.arredondar` aplica 2 casas HALF_UP, padrão
comercial; teste `cupomPercentualArredondaMeioParaCima` (40,50 × 10% = 4,05).

**8. "Por que o front-end não calcula o total?"**
Para não haver duas fontes de verdade. O preço vem de `POST /api/catalogo/montagens` e os totais de
`PedidoResponse`. O pedido-service ignora qualquer preço vindo do cliente — `NovoItemRequest` nem tem campo de preço.

**9. "Onde está o Open/Closed?"**
Nova linha = nova classe `FabricaLinha` com `@Component`; `FabricasPorLinha` a recebe na lista do
construtor. Novo meio de pagamento = nova subclasse de `ProcessadorPagamento`.

**10. "Por que não mensageria?"**
O cliente está parado na frente do totem esperando a resposta; a orquestração síncrona com compensação
resolve com menos infraestrutura (ADR-006). Mensageria entraria para notificações assíncronas (ex.:
painel da cozinha) — não é requisito.

**11. "Como os testes de integração entre serviços funcionam sem subir tudo?"**
`ContratosHttpTest` usa `MockRestServiceServer` para verificar o JSON enviado e a interpretação de
201/409/422/503/erro de rede. O teste com tudo de pé é o `smoke-test.sh`.

**12. "O desfazer funciona se o servidor reiniciar?"**
Não, e isso é uma decisão documentada (ADR-005): o pedido persiste, o histórico de desfazer é da sessão.
O backlog B22 descreve como persistir os snapshots.
