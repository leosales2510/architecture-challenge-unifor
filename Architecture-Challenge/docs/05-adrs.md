# 5. Architecture Decision Records (ADRs)

Formato: Contexto → Decisão → Consequências. Status de todos: **Aceito** (Entrega 1).

## ADR-001 — Quatro microsserviços divididos por propriedade de dados
- **Contexto:** o enunciado exige ≥ 4 microsserviços; o risco é dividir um monólito por camadas ou por telas.
- **Decisão:** catálogo, estoque, pagamento e pedido, cada um dono de um conjunto de dados que muda por
  motivos diferentes (ver `02-arquitetura.md` §2.2). Cupons ficam no pedido; não há serviço de usuários.
- **Consequências:** (+) cada serviço é testável e implantável sozinho; (+) o domínio original continua
  reconhecível. (−) o checkout passa a ser distribuído e exige compensação (ADR-006).

## ADR-002 — Service Layer em vez de MVC dentro dos serviços
- **Contexto:** o enunciado permite MVC ou camadas de serviço.
- **Decisão:** Service Layer: `api → service/facade → domain ← repository/client`.
- **Consequências:** (+) não há views no back-end — a view é o React, em outro processo; (+) regras no
  domínio, testáveis sem HTTP. (−) mais pacotes por serviço.

## ADR-003 — Contratos REST duplicados por serviço, sem biblioteca compartilhada
- **Contexto:** os serviços trocam DTOs (montagem, reserva, pagamento).
- **Decisão:** cada consumidor declara os records que usa (`pedido-service/.../client/dto/ContratosRemotos.java`)
  e ignora campos desconhecidos. Testes de contrato com `MockRestServiceServer`.
- **Consequências:** (+) nenhum acoplamento de build entre serviços; um serviço pode evoluir seu modelo
  interno. (−) mudança incompatível no JSON precisa ser coordenada (detectada pelos testes de contrato
  do consumidor e pelo `smoke-test.sh`).

## ADR-004 — Um banco H2 em memória por serviço
- **Contexto:** "propriedade dos dados por serviço" e facilidade de demonstração no Mac.
- **Decisão:** H2 em memória com JPA (`ddl-auto: create-drop`) e carga inicial em Java (`CargaInicial*`).
- **Consequências:** (+) zero instalação, estado limpo a cada execução, console H2 para mostrar os bancos
  separados. (−) dados não persistem entre reinícios; em produção troca-se a URL para PostgreSQL (o código
  JPA não muda) e `ddl-auto` por migrações (Flyway) — próximo passo (B24).

## ADR-005 — Histórico de desfazer em memória no pedido-service
- **Contexto:** o Command guarda o Memento (snapshot) para desfazer; o pedido em si está no banco.
- **Decisão:** `HistoricosPorPedido` mantém um `HistoricoComandos` por pedido em um `ConcurrentHashMap`,
  limitado a 20 comandos, descartado quando o pedido é pago ou cancelado.
- **Consequências:** (+) simples e fiel ao padrão original; o desfazer é uma funcionalidade da sessão do
  totem. (−) se o pedido-service reiniciar, o pedido continua salvo mas o desfazer recomeça vazio; com
  várias instâncias do serviço seria preciso persistir os snapshots (ex.: JSON numa tabela).

## ADR-006 — Checkout como saga orquestrada por um Mediator, com REST síncrono
- **Contexto:** reserva de estoque e pagamento estão em serviços diferentes; não há transação distribuída.
- **Decisão:** `TotemCheckoutMediator` orquestra: reservar (baixa o saldo) → cobrar → confirmar, ou
  liberar a reserva como compensação. Sem mensageria.
- **Consequências:** (+) o cliente recebe a resposta final na mesma requisição; (+) regra de coordenação
  num único lugar. (−) se a liberação falhar, a reserva fica `PENDENTE` (logada) — mitigação planejada:
  expiração automática de reservas pendentes (próximo passo B21). Idempotência no pagamento (ADR-008).
- **Revisão (auditoria pré-entrega):** a compensação só é aplicada quando o pagamento comprovadamente
  não ocorreu. Resultado incerto (timeout de leitura, 5xx desconhecido) leva o pedido a
  `PAGAMENTO_EM_VERIFICACAO`, mantendo a reserva; a reconciliação conclui apenas com aprovação
  comprovada. (−) um pedido incerto sem aprovação fica bloqueado até decisão manual (B29).

## ADR-007 — Sem API Gateway nem autenticação; proxy do Vite no desenvolvimento
- **Contexto:** o totem é anônimo; o foco da disciplina é a arquitetura dos serviços.
- **Decisão:** o front-end acessa os serviços pelo proxy do Vite (`vite.config.js`), evitando CORS.
- **Consequências:** (+) menos infraestrutura para instalar e explicar. (−) em produção seria necessário
  um gateway (Spring Cloud Gateway/Nginx) e proteção do endpoint de ajuste de estoque.

## ADR-008 — BigDecimal, HALF_UP e idempotência de pagamento
- **Contexto:** o original usava `double`; pagamentos podem ser repetidos após timeout.
- **Decisão:** `BigDecimal` escala 2, `RoundingMode.HALF_UP` (`Dinheiro.arredondar`); o adaptador
  converte para centavos com `longValueExact()`; o pagamento-service devolve o pagamento aprovado
  existente (HTTP 200) se o mesmo `pedidoId` for cobrado de novo.
- **Consequências:** (+) totais exatos (ex.: 40,50 × 10% = 4,05); um pedido já pago não é cobrado de
  novo. (−) o `pedidoId` vira a chave de idempotência — suficiente para um totem.
- **Revisão (auditoria pré-entrega):** a consulta "já existe aprovado?" sozinha não protegia chamadas
  simultâneas (teste `PagamentoConcorrenteTest` reproduziu 2 cobranças). Agora consulta, cobrança e
  gravação ocorrem sob uma trava por pedido (64 travas "listradas", memória fixa), solta só após o
  commit. (−) vale para uma instância do serviço; com réplicas seria preciso trava no banco
  (`SELECT ... FOR UPDATE` numa linha por pedido ou índice único) — backlog B28.

## ADR-009 — Spring Boot 3.5 com Java 17
- **Contexto:** Spring Boot 4.x já existe; os materiais de estudo e respostas da comunidade são, na
  maioria, da linha 3.x.
- **Decisão:** Spring Boot 3.5.14, `java.version` 17 (compila e roda também em JDK 21/24/25).
- **Consequências:** (+) APIs estáveis e conhecidas (`@MockitoBean`, `RestClient`, `ProblemDetail`).
  (−) migração para 4.x fica como evolução futura.
