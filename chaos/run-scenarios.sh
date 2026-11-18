#!/usr/bin/env bash
# Dispara todos os cenários de chaos contra o serviço, em sequência.
# Uso: CHAOS_JWT_SECRET=... ./chaos/run-scenarios.sh  (ou CHAOS_JWT=<token pronto>)
# Requer `mvn package` antes, se o token for gerado aqui.
set -euo pipefail
url="${URL:-http://127.0.0.1:${PORT:-8080}}"
token="${CHAOS_JWT:-$(java -cp target/demo-service.jar labs.demo.lib.Jwt)}"
for name in $(curl -sSf -H "Authorization: Bearer $token" "$url/chaos/scenarios" | grep -o '"[a-z0-9-]*"' | tr -d '"'); do
  code=$(curl -sS -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $token" \
    -H 'Content-Type: application/json' -d "{\"name\":\"$name\"}" "$url/chaos/scenario")
  echo "$name $code"
done
