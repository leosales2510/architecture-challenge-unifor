# 12. Checklist das pendências da entrega definitiva (E-1 + E-2 · 03/11/2026)

Estas tarefas são **manuais e da equipe**. Nenhuma está concluída: só marque `[x]` quando houver a
evidência indicada, guardada em `docs/evidencias/` (crie a pasta) ou nos slides.

## 1. Diagrama de arquitetura no MDL (mdlmodel.com) — B04
- [ ] Conta criada e modelo novo aberto no mdlmodel.com
- [ ] 12 elementos de `docs/10-modelo-mdl.md` desenhados (2 atores, 1 sistema externo, front-end, 4 serviços, 4 bancos)
- [ ] 10 relações com os rótulos da tabela (incluindo pedido → catálogo / estoque / pagamento)
- [ ] Padrões anotados em cada serviço e a nota "um banco por serviço"
- [ ] Conferido contra `docs/diagramas/arquitetura-microsservicos.drawio` (mesmos elementos)
- **Evidência:** link compartilhável do modelo **e** imagem exportada (`docs/evidencias/mdl-arquitetura.png`)

## 2. Ferramenta de gerenciamento (Notion, Asana ou Trello) — B18
- [ ] Ferramenta escolhida: ______________
- [ ] `docs/backlog/backlog.csv` importado (29 itens) ou cadastrado manualmente
- [ ] Colunas/status: A fazer · Fazendo · Feito
- [ ] Cada cartão com responsável **real** (não "Membro N")
- [ ] Itens que dizem "Feito" no CSV foram revisados pela equipe antes de ficarem em "Feito" (o CSV reflete a implementação, não o trabalho de cada pessoa)
- [ ] Todos os integrantes adicionados ao quadro
- **Evidência:** print do quadro com data visível e link do quadro (`docs/evidencias/quadro-backlog.png`)

## 3. Integrantes e responsabilidades — B20
- [ ] Nomes reais inseridos em `docs/08-plano-de-trabalho.md` §8.1 (substituir "Membro 1…5")
- [ ] Se a equipe tiver 4 pessoas: redistribuição do Membro 5 aplicada (§8.1)
- [ ] Cada integrante rodou o projeto no próprio Mac (`./scripts/start-all.sh`, `mvn test`)
- [ ] Cada integrante consegue explicar a própria parte e responder às perguntas de `docs/09` §9.3 da sua área
- [ ] Cada integrante fez ao menos uma alteração pequena na própria parte (ex.: nova cobertura, novo cupom) e a desfez
- **Evidência:** tabela de §8.1 com nomes + cartões atribuídos na ferramenta (se houver repositório Git: commits de cada um)

## 4. Slides da entrega definitiva — B19
- [x] Os 15 slides de `docs/09` §9.1 montados (.key, .pptx e .pdf, com notas do apresentador)
- [ ] Imagens: MDL (item 1), diagramas de casos de uso, classes (`classes-padroes.drawio`) e sequência. Já no deck: casos de uso (slide 3) e arquitetura (slide 4); classes e sequência estão em `ENTREGA_FINAL/diagramas_png/`; falta a imagem do MDL
- [x] Trechos de código dos 9 padrões (um por slide de categoria: slides 8, 9 e 10)
- [ ] Print do quadro de backlog (item 2) e tabela de responsabilidades com nomes (item 3)
- [ ] Print dos resultados: `mvn test` (85 testes, BUILD SUCCESS) e `smoke-test.sh` (22/22), tirados **pela equipe**
- [x] Slide de limitações (seção final de `docs/11`): slide 14
- [ ] Ensaio cronometrado: ______ min (alvo: 15–20)
- **Evidência:** `ENTREGA_FINAL/Apresentacao_Architecture_Challenge.pdf` (15 páginas)

## 5. Demonstração no navegador — B15
Em 29/10/2026 o roteiro completo foi percorrido por script num Chrome real (puppeteer), sem erros de
JavaScript; as 12 capturas estão em `ENTREGA_FINAL/capturas/`. Falta o **ensaio humano** no computador da apresentação.
- [ ] `./scripts/start-all.sh` sem erros; `cd frontend && npm install && npm run dev`
- [ ] `http://localhost:5173` abre o cardápio, sem erros no console do navegador (F12)
- [ ] Roteiro de `docs/09` §9.2, passos 1 a 10, executado do início ao fim
- [ ] Passo 8 (cartão recusado): a trilha mostra `ESTOQUE_LIBERADO` e o painel de estoque devolve o saldo
- [ ] Testado em largura de celular (DevTools → modo responsivo)
- [ ] Plano B testado: o mesmo roteiro via `docs/api/requests.http` ou `scripts/smoke-test.sh`
- [ ] Problemas encontrados anotados e corrigidos (ou levados como limitação)
- **Evidência:** gravação de tela curta ou prints dos passos 2, 6, 8 e 10 (`docs/evidencias/demo-*.png`)

## Situação atual
| Pendência | Status | Evidência existente |
|---|---|---|
| MDL | ⛔ não iniciado | nenhuma |
| Ferramenta de gestão | ⛔ não iniciado | apenas o CSV pronto para importação |
| Integrantes | ⛔ não iniciado | papéis definidos sem nomes |
| Slides | 🟡 prontos (.key, .pptx, .pdf); faltam nomes e ensaio | `ENTREGA_FINAL/Apresentacao_Architecture_Challenge.*` |
| Demonstração no navegador | 🟡 percorrida por script; falta ensaio da equipe | `ENTREGA_FINAL/capturas/` (12 capturas reais) |
| Ensaio geral cronometrado no MacBook da apresentação (B27) | ⛔ não iniciado | nenhuma |
