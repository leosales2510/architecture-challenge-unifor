# Checklist da entrega final — Architecture Challenge (Grupo 3)

Entrega definitiva: **terça-feira, 03/11/2026**, com a E-1 (Plano de Trabalho + MVP) e a E-2 (Apresentação Final) apresentadas juntas. Legenda: ✅ pronto e verificado · 🟡 pronto, mas falta uma ação da equipe · ⛔ depende só da equipe.
Nenhum item manual foi marcado como feito sem evidência.

## A. Requisitos do enunciado (PDF do professor)

| Requisito | Status | Evidência |
|---|---|---|
| Evoluir o sistema anterior (domínio, cenário e regras) | ✅ | `docs/01-diagnostico-e-conformidade.md`; original intacto em `original/` |
| ≥ 4 microsserviços independentes | ✅ | `catalogo-service`, `estoque-service`, `pagamento-service`, `pedido-service` (pom, porta e banco próprios) |
| Back-end em Java + Spring Boot | ✅ | Spring Boot 3.5.14, Java 17 (`*/pom.xml`) |
| APIs REST | ✅ | 25 endpoints — `docs/06-api.md`, `docs/api/requests.http` |
| Arquitetura interna MVC ou Service Layer, justificada | ✅ | Service Layer — `docs/02-arquitetura.md` §2.3, ADR-002 |
| 3 criacionais + 3 estruturais + 3 comportamentais, justificados | ✅ | `docs/03-design-patterns.md`; slides 8–10; `grep -rn "PADRÃO:" */src/main` |
| SOLID e GRASP evidentes no código | ✅ | `docs/04-solid-grasp.md`; slide 11 |
| Front-end consumindo as APIs | ✅ | `frontend/`; percorrido num Chrome real por script (12 capturas em `capturas/`) |
| Diagramas arquiteturais e divisão de módulos | ✅ | `docs/diagramas/arquitetura-microsservicos.drawio` + PNG; slides 4–5 |
| UML: casos de uso, classes, sequência | ✅ | `docs/diagramas/*.drawio` + Mermaid; PNG em `diagramas_png/`; slide 3 |
| ADRs (detalhamento das decisões) | ✅ | `docs/05-adrs.md` (9 ADRs); slide 7 |
| **Diagrama de arquitetura no MDL (mdlmodel.com)** | ⛔ | **Não criado na plataforma.** Elementos e relações prontos em `docs/10-modelo-mdl.md` |
| **Uso de ferramenta de gestão (Trello/Notion/Asana)** | ⛔ | **Não há quadro criado.** Backlog pronto para importar: `docs/backlog/backlog.csv` (29 itens) |
| Backlog das atividades | ✅ | `docs/backlog/backlog.csv`, `docs/08-plano-de-trabalho.md` |
| Divisão de responsabilidades (membros) | 🟡 | Papéis definidos em `docs/08` e no roteiro; **faltam os nomes reais** |
| MVP funcional (E-1) | ✅ | fluxo completo; `evidencias/smoke-test_saida.txt` (22/22) |
| Testes unitários e de integração (E-2) | ✅ | 85 testes — `evidencias/mvn-test_resumo.txt` e `evidencias/surefire/` |
| Documentação técnica final (E-2) | ✅ | `README.md` + `docs/01`–`12` (revisão de consistência em 30/10/2026, `docs/11`) |
| Apresentação (slides) | 🟡 | `.key`, `.pptx` e `.pdf` prontos; **faltam nomes na capa e um ensaio** |

## B. Arquivos desta pasta

| Arquivo | Para quê | Verificado |
|---|---|---|
| `Apresentacao_Architecture_Challenge.key` | Editar e apresentar no **Keynote** | Criado pelo Keynote 13.2 a partir do .pptx; 15 slides 16:9; notas do apresentador presentes nos 15 |
| `Apresentacao_Architecture_Challenge.pptx` | Compatibilidade com PowerPoint / Google Slides | Validador OOXML aprovado; aberto pelo Keynote |
| `Apresentacao_Architecture_Challenge.pdf` | Backup para projetar | Exportado pelo Keynote; 15 páginas revisadas visualmente |
| `Roteiro_5_Integrantes.pdf` / `.md` | Fala de cada integrante (= notas dos slides) | Mesmos slides e textos do deck |
| `Guia_Estudos_Perguntas.pdf` / `.md` | Estudo rápido + 12 perguntas com resposta | Caminhos de classe conferidos no código |
| `Roteiro_Demonstracao.md` | Cliques exatos, comandos e plano B | Valores observados na execução real |
| `capturas/` | 12 capturas reais do sistema (plano B) | Geradas por script no Chrome, 29/10/2026 |
| `diagramas_png/` | 4 diagramas renderizados dos .drawio | Revisados visualmente |
| `evidencias/` | Saídas reais de `mvn test` e `smoke-test.sh` | Execução de 29/10/2026 (JDK 25.0.2); `mvn test` reexecutado em 30/10/2026 com o mesmo resultado |
| `docs/` | Cópia da documentação técnica do projeto | — |

## C. Ações manuais indispensáveis (antes de terça)

### 1. Nomes dos integrantes (10 min) ⛔
- [ ] Keynote: slide 1, trocar `[Integrante 1] … [Integrante 5]` pelos nomes
- [ ] `Roteiro_5_Integrantes.md` e `docs/08-plano-de-trabalho.md` §8.1: trocar "Integrante/Membro N" pelos nomes
- [ ] Notas dos slides 1, 3, 7 e 11 mencionam `[nomes]` e `[Integrante 2/3/4]`: trocar pelos nomes

### 2. Diagrama no MDL (1–2 h) ⛔
- [ ] Criar o modelo em https://mdlmodel.com seguindo `docs/10-modelo-mdl.md` (12 elementos, 10 relações)
- [ ] Exportar imagem para `docs/evidencias/mdl-arquitetura.png` e guardar o link
- [ ] Opcional: trocar a imagem do slide 4 pela exportada do MDL (ou citar o link na nota do slide 4)

### 3. Ferramenta de gestão (30 min) ⛔
- [ ] Notion (Importar → CSV) ou Asana (Importar → CSV) com `docs/backlog/backlog.csv`; no Trello, cadastrar os cartões
- [ ] Atribuir cada cartão a um integrante real; colunas A fazer / Fazendo / Feito
- [ ] Print do quadro com data visível → acrescentar um slide ou mostrar ao vivo se o professor pedir

### 4. Ensaio no MacBook da apresentação (30–45 min) ⛔
- [ ] Instalar Java 17+, Maven e Node no Mac que será usado (`README.md`)
- [ ] `./scripts/start-all.sh` + `cd frontend && npm install && npm run dev`
- [ ] Executar `Roteiro_Demonstracao.md` do início ao fim
- [ ] Ensaio cronometrado dos 15 minutos com os 5 integrantes
- [ ] Cada integrante abre as classes da sua parte (lista no roteiro) e responde às perguntas do guia

### 5. No dia
- [ ] Levar o PDF e as capturas num pendrive (plano B sem internet)
- [ ] Subir os serviços 10 minutos antes; abrir o Keynote em modo apresentação com notas

## D. Limitações conhecidas (declaradas nos slides e na documentação)
- Bancos H2 em memória (ADR-004); histórico de desfazer em memória (ADR-005).
- Pedido em `PAGAMENTO_EM_VERIFICACAO` sem aprovação registrada exige decisão manual (B29).
- Trava contra cobrança dupla vale para uma instância do pagamento-service (B28).
- Sem autenticação nem API Gateway de produção (ADR-007).
- Front-end sem testes automatizados (B23); foi percorrido por script num Chrome real.
- O código foi desenvolvido com apoio de IA: cada integrante precisa dominar a sua parte (B20).
