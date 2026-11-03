# 8. Plano de trabalho, responsabilidades e cronograma

> Os nomes não foram inventados. Substitua "Membro N" pelos nomes reais e registre a contribuição de
> cada um (cartões na ferramenta de gestão e/ou commits). A rubrica avalia participação individual (0,3 pt na E-1).

> **Formato da entrega:** a E-1 (Plano de Trabalho + MVP) e a E-2 (Apresentação Final) serão
> apresentadas **juntas**, na entrega definitiva de **terça-feira, 03/11/2026**. Os itens de backlog que não
> serão implementados até essa data estão marcados como `Futuro` e aparecem no slide 14 como próximos passos.

## 8.1 Divisão de responsabilidades (segue os papéis do enunciado)

| Membro | Papel no enunciado | Responsável por | Arquivos principais que deve saber explicar |
|---|---|---|---|
| **Membro 1** | Definição e modelagem da arquitetura | Divisão em serviços, ADRs, diagramas (arquitetura, casos de uso, classes, sequência, MDL) | `docs/02`, `docs/05`, `docs/diagramas/*`, `docs/10` |
| **Membro 2** | Padrões estruturais + persistência | Decorator, Adapter, Facade (em conjunto com M4); JPA/Repository e bancos H2; Abstract Factory e Builder do catálogo; Factory Method | `catalogo-service/**`, `pagamento-service/**`, `*/repository/*` |
| **Membro 3** | Padrões comportamentais + controle da interação | Command, Memento, Mediator; estados do pedido; reservas do estoque | `pedido-service/.../command`, `.../checkout`, `.../domain/Pedido.java`, `estoque-service/**` |
| **Membro 4** | Integração de APIs / funcionalidades adicionais | Clientes HTTP, contratos, tratamento de erros, `PedidoFacade`, front-end React | `pedido-service/.../client`, `.../facade`, `.../api`, `frontend/**` |
| **Membro 5** (se houver) | Testes e documentação | Testes JUnit, smoke test, backlog na ferramenta, slides, README | `*/src/test/**`, `scripts/*`, `docs/07`, `README.md` |

Com 4 integrantes: o Membro 5 é absorvido — testes de cada serviço ficam com o dono do serviço, e
backlog/slides com o Membro 1.

> A tabela acima divide o **código** (quem domina cada parte). A divisão da **fala** na apresentação está em
> `ENTREGA_FINAL/Roteiro_5_Integrantes.md` (Integrante 1–5, slides 1–15). Ao inserir os nomes, a equipe decide
> se o Membro N e o Integrante N são a mesma pessoa.

> Observação honesta: o código desta versão foi gerado de uma só vez com apoio de IA. Para que a
> divisão seja real, cada membro deve **revisar, executar e conseguir alterar** a sua parte (B20 no
> backlog) e registrar isso na ferramenta de gestão.

## 8.2 Backlog

Arquivo importável: `docs/backlog/backlog.csv` (29 itens: ID, título, entrega, prioridade, responsável,
estimativa, dependências, status, critério de aceite). Resumo:

| Status | E-1 | E-2 | Futuro (após 03/11) |
|---|---|---|---|
| Feito (implementado e verificado) | 16 (B01–B03, B05–B14, B16, B17, B19 slides) | 1 (B26 documentação) | 0 |
| Fazendo | 1 (B15: percorrido por script no Chrome; falta o ensaio humano) | 0 | 0 |
| A fazer (equipe) | 3 (B04 MDL, B18 ferramenta, B20 nomes/estudo) | 1 (B27 ensaio cronometrado) | 7 (B21–B25, B28, B29) |

### Como colocar na ferramenta de gestão (precisa ser feito pela equipe)
- **Notion:** criar uma página → `Import` → `CSV` → selecionar `backlog.csv`; trocar a visualização para *Board* agrupado por `Status`.
- **Asana:** novo projeto → `Importar` → `CSV` → mapear colunas (Título → nome da tarefa, Responsável, Status → seção).
- **Trello:** a importação de CSV não é nativa no plano gratuito (até onde sei); crie as listas *A fazer / Fazendo / Feito* e cadastre os cartões manualmente, ou use um Power-Up de importação.
- Tire prints do quadro para os slides (é a evidência pedida no enunciado). Nenhuma evidência de uso de ferramenta foi criada por mim.

## 8.3 Cronograma sugerido

Atualizado em 30/10/2026. Entrega definitiva (E-1 + E-2 juntas): **terça-feira, 03/11/2026**.

| Período | Atividades | Itens |
|---|---|---|
| Sex 30/10 – Sáb 31/10 | Inserir os nomes reais (capa e notas dos slides, roteiro, §8.1); importar o backlog na ferramenta e atribuir os cartões | B18, B20 |
| Sáb 31/10 – Dom 01/11 | Diagrama no MDL; cada integrante roda o projeto e estuda a sua parte | B04, B20 |
| Seg 02/11 | Ensaio da demonstração no MacBook da apresentação + ensaio geral cronometrado (15 min) | B15, B27 |
| **Ter 03/11** | **Entrega definitiva (E-1 + E-2):** slides, demonstração ao vivo, testes e documentação | — |
| Após a entrega | Próximos passos declarados no slide 14 | B21–B25, B28, B29 |

## 8.4 Estimativas

Somatório do backlog: ~84 h já feitas (itens `Feito`, referência de esforço) + ~16 h de pendências da
equipe antes de 03/11 (B04, B15, B18, B20, B27) + ~36 h de próximos passos (`Futuro`). Divididas por
5 pessoas, as pendências cabem em cerca de 3 h por pessoa.
