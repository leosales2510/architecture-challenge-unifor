# 1. Diagnóstico do projeto original e matriz de conformidade

## 1.1 O que foi entregue na atividade anterior (Grupo 3)

Material analisado (cópia intacta em `original/Grupo3_Sorveteria/`):

| Item | Conteúdo |
|---|---|
| `sorveteria 2/src/sorveteria/**` | 26 classes Java puras (sem framework), pacote por padrão |
| `sorveteria 2/README.md` | Explicação dos 5 padrões e da ligação Command + Memento |
| `saida_exemplo.txt` | Saída da `Main` com 9 etapas |
| `Caso_de_Uso_Sorveteria.drawio` | Atores Cliente, Atendente, Sistema de Pagamento |
| `Classe_Sorveteria.drawio` | Diagrama de classes dos 5 padrões |
| `Sequencia_Sorveteria.drawio` | Montar sorvete → adicionar → desfazer (22 mensagens) |
| `Grupo3_Codigo_Slides_2.pdf` | 11 slides com trechos de código de cada padrão |

**Verificação executada:** `javac -encoding UTF-8 -d out $(find src -name "*.java") && java -cp out sorveteria.Main`
compilou sem erros (JDK 25) e a saída é **idêntica** a `saida_exemplo.txt` (`diff` sem diferenças).
Não havia nenhum teste automatizado.

### Funcionalidades e regras de negócio originais

- Duas linhas de produto: **Tradicional** (base R$ 9,00 + casquinha R$ 2,00) e **Zero Açúcar** (base R$ 11,00 + casquinha integral R$ 2,50).
- Coberturas: calda de chocolate (+R$ 2,50) e granulado (+R$ 1,50), empilháveis.
- Carrinho: adicionar, remover por posição, aplicar cupom `SORVETE10` (10%), desfazer a última ação, finalizar (só imprime o total).
- Totais: subtotal, desconto, total.

## 1.2 Os 5 padrões estavam corretos?

Verificação pela estrutura e pelo comportamento, não pelo nome das classes:

| Padrão | Veredito | Evidência | Problemas encontrados |
|---|---|---|---|
| **Abstract Factory** | Correto | `FabricaLinha` cria **dois** produtos relacionados (`criarSorvete`, `criarCasquinha`); `FabricaZero` sempre combina `SorveteZero` + `CasquinhaZero`. | A escolha da fábrica era um `if` em `TotemMediator.definirLinha` (`equalsIgnoreCase("Zero")`) — abrir uma linha nova exigia editar o mediador. Construtores dos produtos eram públicos (nada impedia `new SorveteZero(...)` fora da fábrica). |
| **Decorator** | Correto | `CoberturaDecorator implements Sorvete` e guarda um `Sorvete`; empilhamento `new Granulado(new CaldaChocolate(s))` funciona. | Preços fixos no código e em `double`. O `switch` do mediador tinha `default -> item`, que **ignorava em silêncio** coberturas desconhecidas. |
| **Command** | Correto | `Comando` com `executar/desfazer`; os comandos delegam ao receptor (`pedido.adicionar(...)`); `HistoricoComandos` é a pilha. | Um comando era empilhado mesmo que a ação não tivesse efeito; cupom inválido zerava o desconto silenciosamente. |
| **Mediator** | Correto | `PainelCardapio/Carrinho/Total` só conhecem o `Mediator`; o `TotemMediator` decide as reações. | Eventos em `String` (`"linha:Zero"`, `"remover:1"`) sem verificação do compilador; o `TotemMediator` acumulava responsabilidades (escolher fábrica, montar coberturas, criar comandos, imprimir), tendendo a "God class"; a lista `componentes` não era usada. |
| **Memento** | Correto | `SnapshotPedido` com getters package-private: só o `Pedido` lê; o comando guarda o memento sem enxergá-lo. | Nenhum relevante; mantido praticamente igual. |

### Problemas técnicos gerais

1. **Dinheiro em `double`** (`9.00 + 2.50` funciona por sorte; somas e percentuais acumulam erro binário).
2. **Monólito de console** sem persistência, sem API, sem validação de entrada e sem tratamento de erros.
3. Cupom fixo no código (`"SORVETE10".equalsIgnoreCase(codigo)`), sem validade nem valor mínimo.
4. Sem estoque e sem pagamento — "finalizar" apenas imprimia o total, apesar do caso de uso "Realizar pagamento" no diagrama.
5. Nenhum teste automatizado.

## 1.3 Matriz de conformidade com o enunciado

Legenda: ✅ atendido · 🟡 parcial · ⛔ pendente. "Antes" = projeto original; "Agora" = este repositório.

| # | Requisito do professor | Antes | Agora | O que foi feito / o que falta | Arquivo ou componente |
|---|---|---|---|---|---|
| 1 | Reaproveitar domínio, cenário e regras | — | ✅ | Mesmas linhas, preços, coberturas, cupom SORVETE10 e desfazer. O exemplo original (R$ 17,50 / R$ 27,90) é verificado por teste. | `CargaInicialCatalogo`, `CommandMementoTest.roteiroDoProjetoOriginal` |
| 2 | Evolução significativa (não só reproduzir) | ⛔ | ✅ | Estoque com reservas, pagamentos simulados, cupons com regras, quantidades, status do pedido, front-end. | 4 serviços + `frontend/` |
| 3 | Mínimo de 4 microsserviços independentes | ⛔ | ✅ | catálogo, pedido, estoque, pagamento; cada um com `pom.xml`, banco e porta próprios. | `*-service/` |
| 4 | Spring Boot / Java em todos | ⛔ | ✅ | Spring Boot 3.5.14, Java 17 (testado também em JDK 25). | `*/pom.xml` |
| 5 | APIs REST | ⛔ | ✅ | 25 endpoints documentados. | `docs/06-api.md`, `*Controller.java` |
| 6 | Arquitetura interna MVC ou Service Layer, justificada | ⛔ | ✅ | Service Layer em todos os serviços. | `docs/02-arquitetura.md`, ADR-002 |
| 7 | 3 padrões criacionais | 🟡 (1) | ✅ | Abstract Factory, Builder, Factory Method. | `docs/03-design-patterns.md` |
| 8 | 3 padrões estruturais | 🟡 (1) | ✅ | Decorator, Adapter, Facade. | idem |
| 9 | 3 padrões comportamentais | ✅ (3) | ✅ | Command, Memento, Mediator (reposicionado no checkout). | idem |
| 10 | SOLID e GRASP evidentes e justificados | 🟡 | ✅ | Cada princípio com classe e método concretos. | `docs/04-solid-grasp.md` |
| 11 | Front-end consumindo as APIs | ⛔ | ✅ | React + Vite; nenhum preço/total calculado no navegador. Percorrido num Chrome real por script em 29/10/2026, sem erros de JavaScript (12 capturas em `ENTREGA_FINAL/capturas/`). | `frontend/src/api.js` |
| 12 | Diagramas arquiteturais e divisão de módulos | 🟡 | ✅ | Draw.io + Mermaid. | `docs/diagramas/` |
| 13 | UML: caso de uso, classes, sequência | ✅ (do monólito) | ✅ | Atualizados para a nova arquitetura (Draw.io: arquitetura, casos de uso, classes, sequência; Mermaid: classes por serviço, sequência, estados); originais preservados. | `docs/diagramas/` |
| 14 | ADRs | ⛔ | ✅ | 9 ADRs. | `docs/05-adrs.md` |
| 15 | Diagrama de arquitetura em MDL (mdlmodel.com) | ⛔ | 🟡 | **Não foi gerado arquivo MDL.** Há um roteiro de modelagem com todos os elementos e relações; precisa ser desenhado na ferramenta pela equipe. | `docs/10-modelo-mdl.md` |
| 16 | Ferramenta de gestão (Trello/Notion/Asana) | ⛔ | 🟡 | Backlog pronto em CSV para importação. **A importação e o uso real da ferramenta precisam ser feitos pela equipe** — não há evidência de uso ainda. | `docs/backlog/backlog.csv`, `docs/08-plano-de-trabalho.md` |
| 17 | Backlog e cronograma | ⛔ | ✅ | Prioridade, dependências, critérios de aceite e estimativas. | `docs/08-plano-de-trabalho.md` |
| 18 | Divisão de responsabilidades (Membros 1–4/5) | ⛔ | 🟡 | Papéis definidos sem nomes; **a equipe precisa preencher os nomes** e registrar quem fez o quê (commits/cartões). | `docs/08-plano-de-trabalho.md` |
| 19 | MVP funcional (E-1) | ⛔ | ✅ | Fluxo completo de compra rodando nos 4 serviços. | `scripts/smoke-test.sh` (22/22) |
| 20 | Testes unitários e de integração (E-2) | ⛔ | ✅ | 85 testes JUnit 5 + 22 verificações ponta a ponta. | `docs/07-testes.md` |
| 21 | Documentação técnica final (E-2) | ⛔ | ✅ | Arquitetura, padrões, diagramas, ADRs, API, testes. | `docs/` |
| 22 | Apresentação (E-1 + E-2 juntas, 03/11/2026) | 🟡 (slides E-0) | 🟡 | 15 slides prontos (.key, .pptx, .pdf) com notas, roteiro dos 5 integrantes e perguntas prováveis; **faltam os nomes na capa e o ensaio da equipe**. | `ENTREGA_FINAL/`, `docs/09-roteiro-apresentacao.md` |

## 1.4 Limitações conhecidas (declaradas, não escondidas)

- **Front-end:** compila (`npm run build`) e foi percorrido num Chrome real por script em 29/10/2026, sem erros de JavaScript (12 capturas em `ENTREGA_FINAL/capturas/`). Não tem testes automatizados (B23). Falta o ensaio humano no computador da apresentação.
- **Histórico do desfazer em memória** no pedido-service (ADR-005): sobrevive enquanto o serviço estiver no ar.
- **Bancos H2 em memória**: os dados recomeçam a cada reinício (bom para a demonstração; para produção seria PostgreSQL — ADR-004).
- **Sem autenticação** e sem API Gateway dedicado (ADR-007); o proxy do Vite cumpre esse papel no desenvolvimento.
- **Reserva órfã**: se a liberação/confirmação falhar por indisponibilidade do estoque, a reserva fica `PENDENTE` (registrado em log). Expiração automática de reservas é próximo passo do backlog (B21).
