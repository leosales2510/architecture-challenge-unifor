#!/usr/bin/env bash
# Compila (sem testes) e sobe os 4 microsserviços em segundo plano.
# Uso: ./scripts/start-all.sh            (compila e sobe)
#      ./scripts/start-all.sh --no-build (só sobe os .jar já compilados)
set -euo pipefail
cd "$(dirname "$0")/.."

SERVICOS=("catalogo-service:8081" "estoque-service:8082" "pagamento-service:8083" "pedido-service:8084")
mkdir -p logs .pids

if [[ "${1:-}" != "--no-build" ]]; then
  echo ">> Compilando os 4 serviços (mvn package -DskipTests)..."
  mvn -q -DskipTests package
fi

for s in "${SERVICOS[@]}"; do
  nome="${s%%:*}"; porta="${s##*:}"
  if lsof -nP -iTCP:"$porta" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "!! Porta $porta já está em uso — $nome não foi iniciado (rode ./scripts/stop-all.sh)"; continue
  fi
  nohup java -jar "$nome/target/$nome-1.0.0.jar" > "logs/$nome.log" 2>&1 &
  echo $! > ".pids/$nome.pid"
  echo ">> $nome iniciado (pid $!, porta $porta, log em logs/$nome.log)"
done

echo ">> Aguardando os health checks..."
for s in "${SERVICOS[@]}"; do
  nome="${s%%:*}"; porta="${s##*:}"
  for _ in $(seq 1 60); do
    if curl -fs "http://localhost:$porta/actuator/health" >/dev/null 2>&1; then echo "   $nome OK"; continue 2; fi
    sleep 1
  done
  echo "!! $nome não respondeu em 60s — veja logs/$nome.log"; exit 1
done
echo ">> Tudo no ar. Front-end: cd frontend && npm install && npm run dev  (http://localhost:5173)"
