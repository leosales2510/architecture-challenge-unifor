#!/usr/bin/env bash
# Teste ponta a ponta contra os 4 serviços REAIS rodando (./scripts/start-all.sh antes).
# Exercita: catálogo -> pedido -> estoque -> pagamento, incluindo recusa, falta de
# estoque e falha da operadora, e confere o saldo do estoque depois de cada caso.
set -uo pipefail
P=http://localhost:8084/api; E=http://localhost:8082/api/estoque; C=http://localhost:8081/api/catalogo
OK=0; FALHA=0

checa() { # descricao, esperado, obtido
  if [[ "$2" == "$3" ]]; then echo "  [ok]    $1 ($3)"; OK=$((OK+1)); else echo "  [FALHA] $1: esperado '$2', obtido '$3'"; FALHA=$((FALHA+1)); fi
}
req() { # metodo url [json] -> imprime "STATUS<TAB>corpo"
  local corpo; corpo=$(mktemp)
  local st; st=$(curl -s -o "$corpo" -w '%{http_code}' -X "$1" "$2" -H 'Content-Type: application/json' ${3:+-d "$3"})
  printf '%s\t%s' "$st" "$(cat "$corpo")"; rm -f "$corpo"
}
status() { cut -f1 <<<"$1"; }
json() { cut -f2- <<<"$1" | jq -r "$2"; }
saldo() { curl -s "$E/$1" | jq -r .quantidade; }
novo_pedido() { json "$(req POST "$P/pedidos")" .id; }
ITEM='{"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-MOR","coberturas":["COB-CALDA","COB-GRAN"],"quantidade":1}'

echo "1) Catálogo: montagem usa Abstract Factory + Builder + Decorator"
r=$(req POST "$C/montagens" "{\"linha\":\"ZERO_ACUCAR\",\"saborSku\":\"SAB-ZERO-MOR\",\"coberturas\":[\"COB-CALDA\",\"COB-GRAN\"]}")
checa "preço do exemplo original" "17.50" "$(json "$r" .precoUnitario)"
r=$(req POST "$C/montagens" '{"linha":"ZERO_ACUCAR","saborSku":"SAB-TRAD-CHOC"}')
checa "sabor de outra linha" "422" "$(status "$r")"

echo "2) Carrinho: Command + Memento"
id=$(novo_pedido)
r=$(req POST "$P/pedidos/$id/itens" "$ITEM");                 checa "adiciona item" "17.50" "$(json "$r" .total)"
r=$(req POST "$P/pedidos/$id/itens" '{"linha":"ZERO_ACUCAR","saborSku":"SAB-ZERO-LIM","coberturas":[],"quantidade":1}')
checa "segundo item" "31.00" "$(json "$r" .total)"
r=$(req PUT "$P/pedidos/$id/cupom" '{"codigo":"SORVETE10"}'); checa "cupom 10%" "27.90" "$(json "$r" .total)"
r=$(req POST "$P/pedidos/$id/desfazer");                       checa "desfaz cupom" "31.00" "$(json "$r" .total)"
r=$(req POST "$P/pedidos/$id/desfazer");                       checa "desfaz item" "1" "$(json "$r" '.itens|length')"
r=$(req PUT "$P/pedidos/$id/cupom" '{"codigo":"VERAO25"}');   checa "cupom expirado" "422" "$(status "$r")"

echo "3) Checkout aprovado: estoque baixa de forma definitiva"
antes=$(saldo SAB-ZERO-MOR)
r=$(req POST "$P/pedidos/$id/finalizacao" '{"metodo":"CARTAO","numeroCartao":"4111111111111111"}')
checa "HTTP" "200" "$(status "$r")"; checa "status do pedido" "PAGO" "$(json "$r" .pedido.status)"
checa "trilha do Mediator" "CHECKOUT_INICIADO>ESTOQUE_RESERVADO>PAGAMENTO_APROVADO>ESTOQUE_CONFIRMADO>PEDIDO_ATUALIZADO" "$(json "$r" '.trilha|join(">")')"
checa "saldo SAB-ZERO-MOR" "$((antes-1))" "$(saldo SAB-ZERO-MOR)"
r=$(req POST "$P/pedidos/$id/itens" "$ITEM");                 checa "pedido pago é imutável" "409" "$(status "$r")"

echo "4) Checkout recusado: reserva é liberada (compensação)"
id=$(novo_pedido); req POST "$P/pedidos/$id/itens" "$ITEM" >/dev/null
antes=$(saldo COB-CALDA)
r=$(req POST "$P/pedidos/$id/finalizacao" '{"metodo":"CARTAO","numeroCartao":"4000000000000002"}')
checa "HTTP" "402" "$(status "$r")"; checa "motivo" "Saldo insuficiente" "$(json "$r" .detail)"
checa "saldo COB-CALDA devolvido" "$antes" "$(saldo COB-CALDA)"
r=$(req POST "$P/pedidos/$id/finalizacao" '{"metodo":"PIX"}')
checa "nova tentativa com Pix" "PAGO" "$(json "$r" .pedido.status)"

echo "5) Operadora fora do ar: 503 e estoque devolvido"
id=$(novo_pedido); req POST "$P/pedidos/$id/itens" "$ITEM" >/dev/null
antes=$(saldo CASQ-ZERO)
r=$(req POST "$P/pedidos/$id/finalizacao" '{"metodo":"CARTAO","numeroCartao":"4000000000009999"}')
checa "HTTP" "503" "$(status "$r")"; checa "pedido volta ao carrinho" "ABERTO" "$(json "$r" .pedido.status)"
checa "saldo CASQ-ZERO devolvido" "$antes" "$(saldo CASQ-ZERO)"

echo "6) Estoque insuficiente (Castanha começa com 2)"
id=$(novo_pedido)
r=$(req POST "$P/pedidos/$id/itens" '{"linha":"TRADICIONAL","saborSku":"SAB-TRAD-CHOC","coberturas":["COB-CAST"],"quantidade":3}')
checa "HTTP" "409" "$(status "$r")"; checa "SKU em falta" "COB-CAST" "$(json "$r" '.faltas[0].sku')"

echo
echo "Resultado: $OK verificações ok, $FALHA falhas"
[[ $FALHA -eq 0 ]]
