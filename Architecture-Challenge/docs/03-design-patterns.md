# 3. Os nove Design Patterns

| Categoria | Padrão | Serviço | Situação |
|---|---|---|---|
| Criacional | Abstract Factory | catalogo-service | herdado, refatorado |
| Criacional | Builder | catalogo-service | **novo** |
| Criacional | Factory Method | pagamento-service | **novo** |
| Estrutural | Decorator | catalogo-service | herdado, refatorado |
| Estrutural | Adapter | pagamento-service | **novo** |
| Estrutural | Facade | pedido-service | **novo** |
| Comportamental | Command | pedido-service | herdado, adaptado a REST |
| Comportamental | Memento | pedido-service | herdado |
| Comportamental | Mediator | pedido-service | herdado, **reposicionado** no checkout |

Os arquivos de cada padrão começam com um comentário `// PADRÃO: <NOME> — papel: ...`, então
`grep -rn "PADRÃO:" */src/main` lista todos os participantes.

**O que NÃO contamos como padrão nosso:** injeção de dependências, proxies transacionais, os
`JpaRepository` do Spring Data e o `RestClient.Builder` são mecanismos do framework. Eles aparecem no
código, mas não foram contabilizados entre os nove. Também existem dois *Template Methods*
(`ComandoPedido.executar` e `ProcessadorPagamento.processar`) e um uso de polimorfismo do tipo Strategy
(`TipoDesconto`); estão documentados como apoio, sem entrar na contagem.

### Análise crítica da proposta inicial

- **Abstract Factory, Decorator, Command, Memento**: continuam fazendo sentido e foram mantidos.
- **Mediator**: no projeto original ele coordenava *painéis de tela*. Num back-end REST não existem
  painéis — essa coordenação passou a ser do React. Mantê-lo coordenando "painéis" no Java seria
  artificial. O ponto do novo sistema onde vários componentes precisariam se conhecer é o **checkout**
  (estoque ↔ pagamento ↔ pedido), então o Mediator foi reposicionado ali. A estrutura do original foi
  preservada: colegas com `setMediator` que só chamam `notificar(remetente, evento)`.
- **Factory Method**: para não confundir com a Abstract Factory, foi colocado onde há **um único
  produto** (`GatewayPagamento`) escolhido **por herança** (subclasse do processador), e não onde há uma
  família de produtos.
- **Facade**: a crítica comum é "toda classe de serviço é uma fachada". Aqui há um subsistema real por
  trás dela (3 clientes HTTP, comandos, históricos, repositórios, cupons, mediador), e o controller
  depende **somente** dela. Alternativa considerada: Proxy (cache do catálogo) — descartado por não
  resolver um problema existente hoje.

---

## 3.1 Abstract Factory (criacional)

- **Problema:** um sorvete Zero Açúcar nunca pode sair numa casquinha tradicional. Sorvete e casquinha
  formam uma *família* que precisa ser criada de forma consistente.
- **Justificativa:** a fábrica concreta garante a combinação; o cliente escolhe a linha uma vez.
- **Classes:** `FabricaLinha` (fábrica abstrata), `FabricaTradicional`, `FabricaZero` (fábricas
  concretas), `Sorvete`, `Casquinha` (produtos abstratos), `SorveteTradicional`, `SorveteZero`,
  `CasquinhaTradicional`, `CasquinhaZero` (produtos), `SorveteBase` (base comum), `FabricasPorLinha` (seleção).
- **Local:** `catalogo-service/src/main/java/br/unifor/sorveteria/catalogo/domain/abstractfactory/`
- **Melhorias sobre o original:** construtores dos produtos agora são package-private (só a fábrica
  cria); preços vêm do banco via porta `CatalogoItens`; a escolha da fábrica deixou de ser `if` e virou
  um mapa `Linha → FabricaLinha` montado com todas as fábricas registradas no Spring (`FabricasPorLinha`).
- **Uso real:** `MontagemService.montar` → `fabricas.para(request.linha())` → `SorveteBuilder.construir`
  → `fabrica.criarSorvete(sabor)`.
- **Teste:** `PadroesCriacionaisEEstruturaisTest.AbstractFactory`.
- **Como demonstrar:** no totem, troque Tradicional ↔ Zero e mostre que a casquinha e o preço base mudam
  juntos; no código, mostre `FabricaZero.criarSorvete` e o construtor package-private de `SorveteZero`.

## 3.2 Builder (criacional)

- **Problema:** um sorvete personalizado tem partes obrigatórias (linha, sabor) e opcionais (0 a 3
  coberturas) e regras que só fazem sentido com o conjunto completo. No original, essa montagem estava
  dentro do `TotemMediator`.
- **Justificativa:** separa a construção (passo a passo, em qualquer ordem) da representação final e
  concentra as validações em `construir()`: nunca existe um `Sorvete` inválido.
- **Classes:** `SorveteBuilder` (builder), `MontagemService` (diretor/cliente), produto `Sorvete`.
- **Local:** `catalogo-service/.../domain/builder/SorveteBuilder.java`
- **Regras aplicadas em `validar()`:** linha e sabor obrigatórios; sabor do tipo SABOR e da mesma linha;
  no máximo 3 coberturas; sem coberturas repetidas; itens de cobertura precisam ser do tipo COBERTURA.
- **Uso real:** `new SorveteBuilder(coberturas).daLinha(...).comSabor(...).comCobertura(...).construir()`
  em `MontagemService.montar`. O Builder usa a Abstract Factory e depois empilha os Decorators.
- **Por que não é bean Spring:** guarda o estado de uma montagem; cada requisição cria o seu.
- **Teste:** `PadroesCriacionaisEEstruturaisTest.Builder` (inclui o exemplo do projeto original: R$ 17,50).
- **Como demonstrar:** no totem, selecione 4 coberturas — a prévia mostra "Máximo de 3 coberturas por
  sorvete", mensagem que vem do `SorveteBuilder` via HTTP 422.

## 3.3 Factory Method (criacional)

- **Problema:** o algoritmo de cobrança é o mesmo para cartão e Pix (validar → obter gateway → cobrar),
  mas o gateway concreto muda conforme o meio de pagamento.
- **Justificativa:** o *Creator* (`ProcessadorPagamento`) define o algoritmo e delega a criação do
  produto ao método-fábrica `criarGateway()`, implementado pelas subclasses. Um novo meio de pagamento
  = uma nova subclasse, sem tocar no `PagamentoService`.
- **Diferença para a Abstract Factory:** aqui há **um** produto (`GatewayPagamento`) e a variação é por
  **herança** do criador; na Abstract Factory há uma **família** de produtos criada por um **objeto** fábrica.
- **Classes:** `ProcessadorPagamento` (Creator), `ProcessadorCartao`, `ProcessadorPix` (Concrete
  Creators), `GatewayPagamento` (Product), `CartaoGatewayAdapter`, `PixGatewaySimulado` (Concrete Products).
- **Local:** `pagamento-service/.../domain/factorymethod/`
- **Uso real:** `PagamentoService.processar` escolhe o processador pelo `MetodoPagamento` e chama
  `processar()`, que internamente invoca `criarGateway()`.
- **Teste:** `AdapterEFactoryMethodTest.factoryMethodDeCadaProcessadorCriaOGatewayCerto`.
- **Como demonstrar:** pagar com "Pix" e com "Cartão aprovado" e mostrar que o código de autorização
  muda de formato (`PIX-XXXXXXXX` × 6 dígitos), porque gateways diferentes foram criados.

## 3.4 Decorator (estrutural)

- **Problema:** combinações livres de coberturas, cada uma somando descrição, preço e consumo de estoque,
  sem criar uma subclasse para cada combinação.
- **Classes:** `CoberturaDecorator` (decorator base, implementa `Sorvete` e guarda um `Sorvete`),
  `CaldaChocolate`, `Granulado` (originais), `Chantilly`, `CastanhaCaramelizada` (novos),
  `CoberturasDisponiveis` (liga o SKU cadastrado ao decorator concreto).
- **Local:** `catalogo-service/.../domain/decorator/`
- **Melhorias sobre o original:** preço vem do cadastro (BigDecimal); o decorator também acrescenta o
  SKU da cobertura em `getComponentes()` — é assim que o estoque sabe o que baixar; cobertura sem
  decorator passou a ser erro (`ComposicaoInvalidaException`) em vez de ser ignorada.
- **Uso real:** `SorveteBuilder.construir` → `coberturasDisponiveis.aplicar(cobertura, sorvete)`.
- **Teste:** `PadroesCriacionaisEEstruturaisTest.Decorator`.
- **Como demonstrar:** selecione calda e granulado e mostre a descrição "… + calda de chocolate +
  granulado" crescendo na prévia; no código, `new Granulado(new CaldaChocolate(base, ...), ...)`.

## 3.5 Adapter (estrutural)

- **Problema:** a "operadora de cartão" tem um SDK que não controlamos, com interface incompatível:
  inglês, valores em **centavos** (`long`), moeda ISO e respostas por **códigos** ("00", "51", "05"...),
  e lança a própria exceção de timeout.
- **Justificativa:** o resto do sistema fala só a interface `GatewayPagamento`; o adaptador isola a
  tradução num único lugar. Trocar de operadora = escrever outro adaptador.
- **Classes:** `GatewayPagamento` (Target), `CartaoGatewayAdapter` (Adapter), `LegacyCardProcessor`
  (Adaptee, simulado), `PixGatewaySimulado` (implementação nativa, sem adaptação).
- **Local:** `pagamento-service/.../domain/adapter/` e `.../adapter/legado/`
- **O que o adaptador traduz:** `BigDecimal` → centavos; fixa `"BRL"`; código "51" → "Saldo
  insuficiente" etc.; `LegacyTimeoutException` → `GatewayIndisponivelException` (HTTP 503).
- **Teste:** `AdapterEFactoryMethodTest` (aprovação, recusas, conversão exata de R$ 500,00, timeout).
- **Como demonstrar:** cenários "Cartão recusado" (final 0002) e "Operadora fora do ar" (final 9999) no
  modal de pagamento.

## 3.6 Facade (estrutural)

- **Problema:** cada operação do carrinho envolve várias peças: carregar o pedido, chamar o catálogo,
  consultar o estoque, criar e executar o comando, gravar, empilhar no histórico e montar a resposta.
  Sem a fachada, o controller teria de conhecer e orquestrar todas elas.
- **Classes:** `PedidoFacade` (fachada); subsistema: `CatalogoClient`, `EstoqueClient`,
  `HistoricosPorPedido`, `Comando`s, `PedidoRepository`, `CupomRepository`, `CheckoutMediator`.
- **Local:** `pedido-service/.../facade/PedidoFacade.java`
- **Evidência:** `PedidoController` tem uma única dependência (`PedidoFacade`) e cada método dele tem uma linha.
- **Teste:** `PedidoApiIntegrationTest` (exercita a fachada inteira pela API).
- **Como demonstrar:** mostrar lado a lado `PedidoController.adicionarItem` (1 linha) e
  `PedidoFacade.adicionarItem` (carrega, monta via catálogo, verifica estoque, executa comando).

## 3.7 Command (comportamental)

- **Problema:** as ações do carrinho precisam poder ser desfeitas e listadas; quem pede a ação (fachada)
  não deve conhecer os detalhes de como desfazê-la.
- **Classes:** `Comando` (interface), `ComandoPedido` (base com o memento), `AdicionarItemCommand`,
  `RemoverItemCommand`, `AplicarCupomCommand` (comandos concretos), `Pedido` (receiver),
  `HistoricoComandos` (pilha, limitada a 20), `HistoricosPorPedido` (uma pilha por pedido).
- **Local:** `pedido-service/.../command/`
- **Adaptação para REST (decisão consciente):** no original o comando guardava a referência ao `Pedido`
  em memória. Em REST, cada requisição recarrega o pedido do banco — uma referência antiga ficaria
  desatualizada. Por isso o receptor é passado em `executar(Pedido)` / `desfazer(Pedido)`; o comando
  continua encapsulando os parâmetros da ação e o estado para desfazê-la.
- **Melhoria:** se a ação falhar (ex.: cupom abaixo do mínimo), o comando não guarda backup e não entra
  no histórico — no original, o comando era empilhado mesmo sem efeito.
- **Teste:** `CommandMementoTest`, `PedidoApiIntegrationTest.fluxoDoCarrinhoComCupomEDesfazer`.
- **Como demonstrar:** o botão "Desfazer: Aplicar cupom SORVETE10" mostra o nome do comando no topo da
  pilha (`historicoDesfazer`), e o histórico lista a pilha inteira.

## 3.8 Memento (comportamental)

- **Problema:** desfazer exige voltar ao estado anterior do pedido sem expor seus detalhes internos a
  quem guarda esse estado.
- **Classes:** `Pedido` (Originator: `criarSnapshot()`, `restaurar()`), `SnapshotPedido` (Memento),
  `ComandoPedido` / `HistoricoComandos` (Caretakers).
- **Local:** `pedido-service/.../domain/Pedido.java` e `SnapshotPedido.java` (mesmo pacote, de propósito).
- **Encapsulamento verificável:** construtor e acessores de `SnapshotPedido` são package-private; o
  teste `CommandMementoTest.snapshotEhOpaco` verifica por reflexão que não há membros públicos.
- **Detalhe:** o snapshot copia os itens para records imutáveis (`ItemCopia`), independentes da
  entidade JPA; o `codigo` do item é preservado ao restaurar.
- **Teste:** `CommandMementoTest.roteiroDoProjetoOriginal` reproduz as etapas 2–8 da `Main.java` original.
- **Como demonstrar:** adicionar dois itens, aplicar cupom, desfazer três vezes vendo os totais voltarem
  (27,90 → 31,00 → 17,50); remover e desfazer a remoção.

## 3.9 Mediator (comportamental)

- **Problema:** no checkout, estoque, pagamento e pedido reagem uns aos outros (pagamento recusado ⇒
  liberar estoque e marcar o pedido; falta de estoque ⇒ não cobrar). Ligações diretas criariam
  dependências cruzadas entre os três.
- **Classes:** `CheckoutMediator` (interface), `TotemCheckoutMediator` (mediador concreto),
  `Participante` (colega abstrato, equivale ao `Componente` original), `ParticipanteEstoque`,
  `ParticipantePagamento`, `ParticipantePedido` (colegas), `EventoCheckout` (enum, substitui as Strings
  do original), `ContextoCheckout` (estado de uma tentativa).
- **Local:** `pedido-service/.../checkout/`
- **Evidência:** nenhum participante tem referência a outro participante; todos só chamam
  `avisar(evento, ctx)` → `mediator.notificar(...)`. Toda a regra "o que acontece depois de X" está no
  `switch` de `TotemCheckoutMediator.notificar`.
- **Relação com microsserviços:** o mediador é o orquestrador da saga de checkout; as liberações de
  reserva são as compensações — aplicadas só quando o dinheiro comprovadamente não saiu. O evento
  `PAGAMENTO_INCERTO` leva o pedido a `PAGAMENTO_EM_VERIFICACAO` **sem** compensação.
- **Teste:** `TotemCheckoutMediatorTest` (aprovado, recusado, sem estoque, pagamento fora do ar, resultado incerto, estoque fora do ar).
- **Como demonstrar:** o modal de resultado mostra a **trilha** devolvida pelo back-end, ex.:
  `CHECKOUT_INICIADO → ESTOQUE_RESERVADO → PAGAMENTO_RECUSADO → ESTOQUE_LIBERADO → PEDIDO_ATUALIZADO`.
