# 7. Testes e verificação

## 7.1 Ferramentas

- **JUnit 5 + AssertJ + Mockito** (via `spring-boot-starter-test`).
- **Spring Boot Test + MockMvc**: sobe o serviço inteiro (H2 + carga inicial) e faz requisições HTTP simuladas.
- **`@MockitoBean`**: no pedido-service, substitui só os clientes HTTP dos outros serviços.
- **`MockRestServiceServer`**: testes de contrato da comunicação entre serviços (JSON enviado e interpretação das respostas 2xx/4xx/5xx/erro de rede).
- **`scripts/smoke-test.sh`** (bash + curl + jq): teste ponta a ponta contra os 4 serviços reais rodando.

## 7.2 Como executar

```bash
mvn test                      # os 4 serviços (pom agregador na raiz)
cd pedido-service && mvn test # um serviço só
./scripts/start-all.sh && ./scripts/smoke-test.sh   # ponta a ponta
```
Relatórios: `<servico>/target/surefire-reports/`.

## 7.3 Resultados registrados (29/10/2026, macOS, JDK 25.0.2, Maven 3.9.15)

`mvn test` na raiz → **BUILD SUCCESS, 85 testes, 0 falhas, 0 erros**:

| Serviço | Classe de teste | Tipo | Testes |
|---|---|---|---|
| catalogo | `PadroesCriacionaisEEstruturaisTest` (Abstract Factory, Decorator, Builder) | unitário | 10 |
| catalogo | `CatalogoApiIntegrationTest` | integração (MockMvc + H2) | 6 |
| estoque | `EstoqueServiceTest` | integração (serviço + H2, rollback por teste) | 6 |
| estoque | `EstoqueApiIntegrationTest` | integração (MockMvc) | 5 |
| pagamento | `AdapterEFactoryMethodTest` | unitário | 7 |
| pagamento | `PagamentoApiIntegrationTest` | integração (MockMvc) | 5 |
| pagamento | `PagamentoConcorrenteTest` | concorrência (2 threads, operadora lenta) | 1 |
| pedido | `PedidoTest` | unitário (domínio, BigDecimal) | 9 |
| pedido | `CommandMementoTest` | unitário (padrões) | 4 |
| pedido | `TotemCheckoutMediatorTest` | unitário (Mockito) | 6 |
| pedido | `ContratosHttpTest` | contrato HTTP (MockRestServiceServer) | 12 |
| pedido | `PedidoApiIntegrationTest` | integração (MockMvc + `@MockitoBean`) | 14 |
| | | **Total** | **85** |

`scripts/smoke-test.sh` com os 4 serviços no ar → **22 verificações ok, 0 falhas**.

**Reexecução:** em 30/10/2026, `mvn test` rodou de novo (JDK 25.0.2, Maven 3.9.15): 85 testes, 0 falhas,
0 erros. O smoke test não foi reexecutado nessa data.

Verificações manuais adicionais (serviços reais): duas cobranças simultâneas via `curl` para o mesmo
pedido geraram 1 pagamento (respostas 201 e 200); com o pagamento-service derrubado, o checkout
respondeu 503 `FALHA_INTEGRACAO`, devolveu o estoque e reabriu o pedido. Além disso: com o estoque-service derrubado, `POST /api/pedidos/{id}/itens` retornou
`503 {"codigo":"SERVICO_INDISPONIVEL","servico":"estoque"}`; após religar, voltou a funcionar.

**Não testado automaticamente:** o front-end (sem testes de componente/E2E no navegador) — próximo passo B23.

## 7.4 Cobertura das prioridades pedidas

| Prioridade | Onde |
|---|---|
| Cálculo de preço e descontos | `PedidoTest` (HALF_UP 40,50 × 10% = 4,05; mínimo; desconto ≤ subtotal), `Builder.reproduzOExemploDoProjetoOriginal` |
| Combinação de coberturas | `Decorator.coberturasSaoEmpilhaveis...`, `Builder.limitaQuantidade...`, `rejeitaCoberturaRepetida...` |
| Validação de estoque | `EstoqueServiceTest.reservaEhTudoOuNada`, `PedidoApiIntegrationTest.estoqueInsuficienteAoAdicionarRetorna409` |
| Inclusão e remoção | `PedidoApiIntegrationTest.removerItemEDesfazerARemocao` |
| Command e Memento | `CommandMementoTest` (roteiro da `Main.java` original; comando que falha; LIFO; snapshot opaco) |
| Finalização | `TotemCheckoutMediatorTest`, `PedidoApiIntegrationTest.checkoutAprovado` |
| Comunicação entre serviços | `ContratosHttpTest` + `smoke-test.sh` |
| Respostas de erro | 400/404/409/422/503 em todos os `*ApiIntegrationTest` |
| Pagamento recusado | `pagamentoRecusadoLiberaAReservaCompensacao`, `checkoutComPagamentoRecusadoRetorna402ELiberaEstoque`, smoke §4 |
| Resultado financeiro (aprovado / recusado / incerto) | `pagamentoRecusadoSeguidoDeErroInesperadoDevolveOPedidoAoCliente`, `pagamentoAprovadoSeguidoDeErroInesperadoConcluiSemLiberarEstoque`, `resultadoIncertoBloqueiaNovaCobrancaEReconciliaSoComAprovacaoComprovada`, `erroInesperadoDuranteACobrancaEhTratadoComoIncerto`, `pagamentoIncertoMantemAReservaEBloqueiaOPedido`, contratos de timeout / conexão recusada / 500 |
| Idempotência sob concorrência | `PagamentoConcorrenteTest.chamadasSimultaneasDoMesmoPedidoCobramUmaUnicaVez` (repetido 3×) |
| Falhas de integração | `erroInesperadoNoCheckoutNaoDeixaOPedidoTravado`, `pagamentoForaDoAr...`, `estoqueForaDoAr...`, `catalogoForaDoAr...`, `servicoRemotoForaDoArRetorna503`, smoke §5 |
