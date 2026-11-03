# Totem da Sorveteria — Architecture Challenge (Grupo 3 · UNIFOR)

Evolução do totem de autoatendimento da sorveteria (projeto de Design Patterns do Grupo 3) para uma
arquitetura de **4 microsserviços Spring Boot + front-end React**, com **9 padrões GoF**, SOLID/GRASP,
testes automatizados e documentação acadêmica.

**Entrega definitiva:** terça-feira, 03/11/2026, com a E-1 (Plano de Trabalho + MVP) e a E-2 (Apresentação Final) apresentadas juntas.

| Serviço | Porta | Responsabilidade | Padrões |
|---|---|---|---|
| `catalogo-service` | 8081 | cardápio, preços, montagem do sorvete | Abstract Factory · Builder · Decorator |
| `estoque-service` | 8082 | saldos e reservas | — |
| `pagamento-service` | 8083 | cartão e Pix simulados | Factory Method · Adapter |
| `pedido-service` | 8084 | carrinho, cupons, desfazer, checkout | Facade · Command · Memento · Mediator |
| `frontend` | 5173 | interface do totem + painel de estoque | — |

## Pré-requisitos (macOS)

```bash
brew install openjdk@17 maven node jq   # Java 17+ (testado com 25), Maven 3.9+, Node 18+ (testado com 26)
java -version && mvn -v && node -v
```

## Como rodar

```bash
# 1) Back-end: compila e sobe os 4 serviços em segundo plano (logs em logs/)
./scripts/start-all.sh

# 2) Front-end (outro terminal)
cd frontend
npm install        # só na primeira vez
npm run dev        # abra http://localhost:5173

# 3) Encerrar tudo
./scripts/stop-all.sh
```

Rodar um serviço isolado: `cd catalogo-service && mvn spring-boot:run`.
Console H2 de cada serviço: `http://localhost:808X/h2-console` (JDBC `jdbc:h2:mem:<catalogo|estoque|pagamento|pedido>`, usuário `sa`, sem senha).

## Testes

```bash
mvn test                   # 85 testes JUnit 5 nos 4 serviços
./scripts/smoke-test.sh    # 22 verificações ponta a ponta (serviços precisam estar no ar)
```

Cartões de teste: `4111 1111 1111 1111` aprova · `4000 0000 0000 0002` recusa · `4000 0000 0000 9999` simula operadora fora do ar · Pix aprova até R$ 1.000.
Cupons: `SORVETE10` (10%), `ZERO5` (R$ 5 a partir de R$ 20), `VERAO25` (expirado).

## Estrutura

```
catalogo-service/  estoque-service/  pagamento-service/  pedido-service/   ← um projeto Maven por serviço
frontend/          React + Vite (proxy do Vite encaminha /api/* ao serviço dono)
scripts/           start-all.sh · stop-all.sh · smoke-test.sh
docs/              documentação acadêmica (abaixo)
original/          cópia intacta do projeto anterior (código, diagramas, slides)
pom.xml            agregador só de build (os serviços não compartilham código)
```

## Documentação

| Documento | Conteúdo |
|---|---|
| [01 — Diagnóstico e conformidade](docs/01-diagnostico-e-conformidade.md) | análise do projeto original, veredito dos 5 padrões, matriz de requisitos |
| [02 — Arquitetura](docs/02-arquitetura.md) | divisão dos serviços, camadas, comunicação, saga do checkout, erros, dinheiro |
| [03 — Design Patterns](docs/03-design-patterns.md) | os 9 padrões: problema, classes, local, uso, teste, como demonstrar |
| [04 — SOLID e GRASP](docs/04-solid-grasp.md) | princípios com classe e método |
| [05 — ADRs](docs/05-adrs.md) | 9 decisões arquiteturais |
| [06 — APIs](docs/06-api.md) · [requests.http](docs/api/requests.http) | endpoints e exemplos |
| [07 — Testes](docs/07-testes.md) | ferramentas e resultados registrados |
| [08 — Plano de trabalho](docs/08-plano-de-trabalho.md) · [backlog.csv](docs/backlog/backlog.csv) | responsabilidades, backlog, cronograma |
| [09 — Apresentação](docs/09-roteiro-apresentacao.md) | slides, demonstração, perguntas prováveis |
| [10 — Modelo MDL](docs/10-modelo-mdl.md) | elementos para desenhar no mdlmodel.com (**arquivo MDL não gerado**) |
| [11 — Auditoria](docs/11-auditoria-rubrica.md) | autoavaliação pela rubrica |
| [12 — Checklist de pendências](docs/12-checklist-pendencias-e1.md) | pendências manuais e a evidência exigida para cada uma |
| [Diagramas](docs/diagramas/) | `.drawio` (arquitetura, casos de uso, classes, sequência) + Mermaid (classes, sequência, estados) |

## Limitações conhecidas

- Pedido em `PAGAMENTO_EM_VERIFICACAO` sem aprovação registrada fica bloqueado até decisão manual (B29).
- A trava de idempotência do pagamento vale para uma instância do serviço (B28).
- Histórico de desfazer em memória e bancos H2 em memória (ADR-004/005).

## Material da apresentação

A pasta [`ENTREGA_FINAL/`](ENTREGA_FINAL/) reúne os slides (Keynote `.key`, `.pptx` e `.pdf`), o roteiro dos 5 integrantes,
o guia de estudos, o roteiro da demonstração, capturas reais, diagramas em PNG, evidências dos testes e o
[checklist final](ENTREGA_FINAL/Checklist_Entrega_Final.md).

## Pendências conhecidas

Dependem da equipe, antes de 03/11: nomes reais dos cinco integrantes (slides, roteiro e `docs/08`), diagrama
na plataforma MDL, backlog organizado numa ferramenta de gestão (Notion, Asana ou Trello) e ensaio final da
apresentação e da demonstração no MacBook da apresentação. Os slides e a demonstração por script já estão
prontos. Evidências exigidas em `docs/12-checklist-pendencias-e1.md` e `ENTREGA_FINAL/Checklist_Entrega_Final.md`.
