# Roteiro da demonstração (≈ 4 minutos) — [Integrante 4]

Os valores esperados abaixo foram **observados numa execução real** do sistema (29/10/2026), percorrida num Chrome por script; as capturas correspondentes estão em `ENTREGA_FINAL/capturas/`.

## Antes da aula (10 minutos antes)

No Terminal do Mac, na pasta do projeto:

```bash
cd ~/Desktop/Architecture-Challenge          # ou onde o ZIP foi extraído
./scripts/start-all.sh                        # compila e sobe os 4 serviços (1ª vez: ~1–2 min)
cd frontend
npm install                                   # só na primeira vez (precisa de internet)
npm run dev                                   # deixe este terminal aberto
```

Abra **http://localhost:5173** no Chrome ou Safari. Se aparecer um pedido antigo, clique em **Novo pedido**.
Os serviços reiniciam com estoque e cupons originais sempre que você roda `./scripts/stop-all.sh` e `./scripts/start-all.sh` de novo.

Pré-requisitos no Mac: Java 17+, Maven, Node 18+ (ver `README.md`). Teste tudo **no computador da apresentação** antes de terça.

## Passo a passo

| # | Clique exatamente em… | Deve aparecer | Diga | Captura real |
|---|---|---|---|---|
| 1 | Card **Zero Açúcar** → sabor **Morango** → coberturas **Calda de chocolate** e **Granulado** | Prévia: "Sorvete zero açúcar de morango na casquinha integral sem açúcar + calda de chocolate + granulado" · **R$ 17,50 cada** | "Quem calcula é o catálogo: Abstract Factory, Builder e Decorators." | `02-montagem-previa.png` |
| 2 *(erro)* | Marque também **Chantilly** e **Castanha caramelizada** | Prévia em vermelho: **"Máximo de 3 coberturas por sorvete"** | "Regra do Builder, no back-end (HTTP 422)." Depois **desmarque** Chantilly e Castanha. | `03-erro-builder-4-coberturas.png` |
| 3 | **Adicionar ao pedido** → sabor **Limão** → **Adicionar ao pedido** | Dois itens; **Total R$ 31,00** | "Cada ação é um Command guardado numa pilha." | `04-carrinho-dois-itens.png` |
| 4 | Campo de cupom: digite **SORVETE10** → **Aplicar** | Desconto − R$ 3,10 · **Total R$ 27,90** · botão "Desfazer: Aplicar cupom SORVETE10" | "O botão mostra o topo da pilha de comandos." | `05-cupom-aplicado.png` |
| 5 | **↶ Desfazer: Aplicar cupom SORVETE10** | **Total volta a R$ 31,00**, aviso "Última ação desfeita" | "O Memento restaurou a foto do pedido." | `06-desfazer-cupom.png` |
| 6 *(erro, opcional)* | Cupom **VERAO25** → **Aplicar** | Aviso vermelho "Cupom VERAO25 expirou em …" | "Cupom com validade: erro 422 padronizado." | `07-erro-cupom-expirado.png` |
| 7 | **Finalizar · R$ 31,00** → marque **Cartão recusado** → **Pagar** | "Pagamento recusado · Saldo insuficiente" + trilha: checkout iniciado → estoque reservado → pagamento recusado → **estoque liberado** → pedido atualizado | "O Mediator liberou a reserva: compensação da saga." Clique **Voltar ao pedido**. | `08-modal-pagamento.png`, `09-pagamento-recusado-trilha.png` |
| 8 | **Finalizar** → **Cartão aprovado** (já vem marcado) → **Pagar** | "Pedido confirmado!" + código de autorização de 6 dígitos + trilha com **estoque confirmado** | "Pagamento aprovado, reserva confirmada, pedido PAGO." | `10-pagamento-aprovado.png` |
| 9 *(se sobrar tempo)* | **Fazer novo pedido** → abra **Painel do operador · Estoque** no rodapé | Saldos por SKU (na captura: CASQ-ZERO 28 = 30 − 2 casquinhas do pedido pago; a recusa não consumiu nada) | "O estoque é outro serviço, com banco próprio." | `11-painel-estoque.png` |

Tempo estimado: passos 1–2 (60 s) · 3–5 (75 s) · 7–8 (75 s) · comentários finais (30 s).

## Se algo der errado durante a apresentação

| Sintoma | O que fazer (≤ 30 s) |
|---|---|
| Página não abre / "Não foi possível abrir o totem" | Rodar `./scripts/start-all.sh` de novo (portas ocupadas: `./scripts/stop-all.sh` antes) e recarregar a página |
| "Serviço indisponível" ao adicionar | Algum serviço caiu: `./scripts/stop-all.sh && ./scripts/start-all.sh --no-build` |
| Estoque insuficiente inesperado | Estoque gasto em ensaios: reiniciar os serviços (volta ao saldo inicial) ou clicar **+5** no painel de estoque |
| Nada funciona | **Plano B** abaixo |

## Plano B (sem depender do sistema no ar)

1. **Capturas reais** (abrir no Pré-Visualização, em ordem): `ENTREGA_FINAL/capturas/01-cardapio.png` … `10-pagamento-aprovado.png`. Narre a mesma tabela acima.
2. **Resultados reais já salvos:** `ENTREGA_FINAL/evidencias/mvn-test_resumo.txt` (85 testes, BUILD SUCCESS) e `smoke-test_saida.txt` (22/22).
3. **Se só o front-end falhar** (serviços no ar), demonstre pela API no Terminal:

```bash
./scripts/smoke-test.sh                              # 22 verificações narradas, ~10 s

# ou passo a passo:
curl -s -X POST localhost:8081/api/catalogo/montagens -H 'Content-Type: application/json' \
  -d '{"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"]}'
curl -s -X POST localhost:8084/api/pedidos            # anote o "id"
```

4. **Se nem o back-end subir:** `mvn test` não precisa dos serviços no ar (só de Java + Maven) e mostra os 85 testes passando.
