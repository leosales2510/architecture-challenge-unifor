#!/usr/bin/env bash
# Encerra os serviços iniciados por start-all.sh e espera as portas serem liberadas.
cd "$(dirname "$0")/.."
shopt -s nullglob
arquivos=(.pids/*.pid)
[[ ${#arquivos[@]} -gt 0 ]] || { echo "Nenhum serviço registrado em .pids/"; exit 0; }
for f in "${arquivos[@]}"; do
  pid=$(cat "$f")
  if kill "$pid" 2>/dev/null; then
    for _ in $(seq 1 30); do kill -0 "$pid" 2>/dev/null || break; sleep 0.5; done
    echo "parado: $(basename "$f" .pid) (pid $pid)"
  fi
  rm -f "$f"
done
