# 11. Auditoria final pela rubrica do professor

Autoavaliação crítica. "Risco" = o que pode custar pontos se não for resolvido pela equipe.
A E-1 e a E-2 serão apresentadas **juntas** na entrega definitiva de 03/11/2026. Revisado em 30/10/2026.

## E-1 — Plano de Trabalho + MVP (15%)

| Critério (peso) | Estado | Evidência | Risco / o que falta |
|---|---|---|---|
| Arquitetura e Modelagem (4%) | Forte | `docs/02`, 9 ADRs, Draw.io + Mermaid, 4 serviços reais | **Diagrama em MDL não feito** (exigência explícita) — B04 |
| Design Patterns (3%) | Forte | 9 padrões implementados e testados, justificativa e localização em `docs/03` | Ensaiar a diferença AF × FM e a mudança do Mediator |
| SOLID e GRASP (2%) | Forte | `docs/04` com classe/método por princípio | — |
| Plano de Trabalho e Backlog (2%) | Parcial | `docs/08`, `backlog.csv` com dependências, estimativas e critérios | **Sem uso real de Trello/Notion/Asana ainda**; nomes dos membros em branco — B18 |
| MVP (2%) | Forte | fluxo completo nos 4 serviços; smoke 22/22; front-end percorrido num Chrome real por script (12 capturas em `ENTREGA_FINAL/capturas/`) | Falta o ensaio humano no MacBook da apresentação — B15 |
| Apresentação e Justificativas (2%) | Bom; falta a equipe | 15 slides (.key/.pptx/.pdf) com notas, `Roteiro_5_Integrantes`, guia com 12 perguntas | **Nomes dos integrantes na capa**; cada membro precisa dominar sua parte — B20 |

## E-2 — Apresentação Final (25%) — apresentada junto com a E-1

| Critério (peso) | Estado | Evidência | O que melhoraria |
|---|---|---|---|
| Funcionamento e Integração (6%) | Bom | front + 4 serviços integrados; falhas tratadas | testes E2E no navegador (B23) |
| Arquitetura de Microsserviços (4%) | Bom | banco por serviço, contratos, saga com compensação, idempotência | expiração de reservas pendentes (B21); gateway em produção |
| Design Patterns (4%) | Bom | todos usados no fluxo real, não só em exemplos | — |
| SOLID, GRASP e Qualidade (3%) | Bom | camadas consistentes, nomes do domínio, sem duplicação relevante | — |
| Testes e Qualidade (3%) | Bom | 85 testes (unitário, integração, contrato) + smoke | front-end sem testes (B23); sem relatório de cobertura (JaCoCo seria simples de acrescentar) |
| Documentação Técnica (2%) | Bom | `docs/01`–`12`, diagramas atualizados; revisão de consistência em 30/10/2026 (B26) | — |
| Apresentação e Demonstração (3%) | Bom; falta a equipe | slides, roteiro de fala, roteiro da demonstração e capturas reais | ensaio cronometrado no MacBook da apresentação (B27) |

## Pontos fracos que um professor exigente pode apontar

1. **Facade pode ser vista como "um service comum".** Defesa: subsistema real por trás, controller só a conhece (`docs/03` §3.6).
2. **Command com receptor por parâmetro.** Defesa em `docs/09`, pergunta 4.
3. **H2 em memória** não demonstra persistência durável. Defesa: ADR-004; trocar para PostgreSQL é configuração.
4. **Estoque sem padrão GoF próprio.** É intencional: é o serviço mais simples (CRUD + reserva transacional); padrões não foram forçados onde não há problema a resolver.
5. **Código gerado com apoio de IA.** A rubrica avalia domínio e participação individual: a equipe deve conhecer o código a ponto de alterá-lo ao vivo (ex.: adicionar uma cobertura nova — classe decorator + entrada em `CoberturasDisponiveis` + linha na carga inicial do catálogo e do estoque).

## Auditoria final (pré-entrega) — verificações e correções

**Verificado sem problemas:** os 9 padrões têm marcadores `// PADRÃO:` e participantes conforme o GoF
(`grep -rn "PADRÃO:" */src/main`); 24 endpoints no código = 24 documentados; todas as classes citadas
em `docs/02`–`04` e `09` existem; nenhum link interno quebrado nos `.md`; 9 ADRs coerentes com o código;
os 4 `.drawio` têm XML válido; as 48 classes do diagrama de classes existem no código.

**Problemas reais encontrados e corrigidos:**

| # | Problema | Correção | Evidência |
|---|---|---|---|
| 1 | Exceção inesperada dentro do checkout deixava o pedido preso em `AGUARDANDO_PAGAMENTO` (sem editar nem cancelar) e a resposta saía fora do padrão RFC 7807 | `PedidoFacade.recuperarCheckoutInterrompido` (libera a reserva e devolve ao carrinho se nada foi cobrado) + tratador genérico 500 `ERRO_INTERNO` que preserva 404/405 do Spring + 400 `PARAMETRO_INVALIDO` | teste `erroInesperadoNoCheckoutNaoDeixaOPedidoTravado` (falhava antes, passa agora) e asserções 400/404/405 em `validacoesDeEntrada` |
| 2 | Em modo dev, o `React.StrictMode` executava a inicialização 2× e abria **dois pedidos** no primeiro acesso | guarda com `useRef` em `frontend/src/App.jsx` | `npm run build` ok |
| 3 | Diagrama de classes atualizado existia só em Mermaid | `docs/diagramas/classes-padroes.drawio` | XML válido, 48 classes conferidas |
| 4 | Pequenos: aviso "Última tentativa de pagamento" também aparecia para falta de estoque; nome de teste com erro de digitação; comentário de teste enganoso; menção a uma classe `HttpClient` inexistente em `docs/04` | textos corrigidos | — |

**Continua pendente (depende da equipe):** MDL, ferramenta de gestão com evidência, nomes dos integrantes e ensaio final no MacBook da apresentação. Os slides ficaram prontos e a demonstração foi percorrida por script num Chrome real em 29/10/2026.

## Segunda auditoria independente — resultado financeiro e concorrência

Cada problema foi reproduzido primeiro por um teste que falhava; depois veio a correção mínima.

| # | Problema apontado / encontrado | Reprodução (falhava antes) | Correção | Estado |
|---|---|---|---|---|
| 1 | Pagamento **recusado** seguido de exceção inesperada: a recuperação era pulada porque `pagamentoId != null`, e o pedido ficava preso em `AGUARDANDO_PAGAMENTO` com a reserva retida | `pagamentoRecusadoSeguidoDeErroInesperadoDevolveOPedidoAoCliente` (status era `AGUARDANDO_PAGAMENTO`) | `SituacaoPagamento` no `ContextoCheckout`; a recuperação decide pelo que se sabe do dinheiro (ver `docs/02` §2.6) | corrigido |
| 2 | (encontrado ao investigar o item 1) **Timeout de leitura** na cobrança era tratado como "serviço indisponível": o estoque era liberado e o pedido reaberto, mesmo com a cobrança possivelmente feita | `timeoutDeLeituraNoPagamentoNaoEhTratadoComoNaoCobrado` | `PagamentoHttpClient` separa "comprovadamente não cobrado" (4xx, 503 `GATEWAY_INDISPONIVEL`, conexão recusada) de "incerto" (timeout de leitura, outros 5xx, resposta ilegível) → novo status `PAGAMENTO_EM_VERIFICACAO`, sem liberação, sem nova cobrança, sem cancelamento; `POST /reconciliacao` conclui só com aprovação comprovada | corrigido |
| 3 | Duas cobranças **simultâneas** do mesmo pedido passavam juntas pela consulta de idempotência | `PagamentoConcorrenteTest` (operadora chamada 2×) | Trava por pedido em `PagamentoService.processar`, solta só após o commit (método não transacional; cada gravação comita sozinha) | corrigido para 1 instância; várias instâncias → B28 |

Regressão: os fluxos já aprovados continuam iguais (smoke 22/22, incluindo "operadora fora do ar" →
503 com estoque devolvido, e pagamento-service desligado → conexão recusada → estoque devolvido).
Agora são 25 endpoints (+ `/reconciliacao`) e 85 testes.

**Limitações conhecidas que permanecem:**
- Pedido em verificação **sem** aprovação registrada fica bloqueado até decisão manual: não existe tela de
  atendente nem consulta à operadora por id de tentativa (B29). Isso é intencional: sem prova, o sistema não libera estoque.
- Trava de idempotência só dentro de uma instância (B28).
- O 503 `GATEWAY_INDISPONIVEL` é tratado como "não cobrado" porque, na simulação, o SDK falha antes de autorizar;
  com uma operadora real, esse caso também seria incerto.
- Se a liberação de uma reserva falhar (estoque fora do ar), ela fica `PENDENTE` (B21).

## Revisão de consistência da documentação (30/10/2026)

Sem mudanças no código, nos 9 padrões nem no front-end. Correções feitas apenas na documentação:

| # | Inconsistência | Correção |
|---|---|---|
| 1 | Documentos tratavam E-1 e E-2 como entregas separadas | `docs/08`, backlog, README e checklists: E-1 + E-2 juntas em 03/11; itens não implementados marcados `Futuro` |
| 2 | Esta auditoria e `docs/01` diziam que os slides não existiam e que o front-end não tinha sido aberto num navegador | textos atualizados (slides prontos em 29/10; front-end percorrido por script no Chrome) |
| 3 | `docs/09` §9.1 descrevia uma ordem de slides de planejamento diferente do deck final | §9.1 reescrita igual aos 15 slides e ao `Roteiro_5_Integrantes` |
| 4 | O cabeçalho de `evidencias/mvn-test_resumo.txt` dizia `java version "24.0.2"`, mas os relatórios Surefire registram que os testes rodaram no JDK 25.0.2 (Homebrew), o JDK usado pelo Maven | cabeçalho corrigido; resultados preservados sem alteração |
| 5 | Backlog com B19 (slides) e B26 (documentação) em "A fazer" | marcados `Feito`; B15 em `Fazendo` |

`mvn test` reexecutado em 30/10/2026: 85 testes, 0 falhas, 0 erros (JDK 25.0.2, Maven 3.9.15).
