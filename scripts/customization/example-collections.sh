#!/usr/bin/env bash
# Explicit API operations for local UaRD layout fixtures. Never run with bash -x.
set -euo pipefail
MODE="${1:-}"
case "$MODE" in create|publish|feature|list) ;; *)
  echo 'Usage: bash scripts/customization/example-collections.sh {create|publish|feature|list}' >&2
  exit 2 ;;
esac
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
DATA="$ROOT/customization/homepage/content/example-collections.json"
SERVER_URL="${SERVER_URL:-http://localhost:8080}"
SERVER_URL="${SERVER_URL%/}"
PARENT="${PARENT:-:root}"
if [[ "$MODE" == list ]]; then
  curl --fail-with-body --silent --show-error "$SERVER_URL/api/dataverses/$PARENT/featured"
  printf '\n'
  exit 0
fi
: "${API_TOKEN:?Set API_TOKEN locally; do not commit or share it}"
if [[ "$MODE" == create ]]; then
  : "${CONTACT_EMAIL:?Set a real collection contact email}"
  export CONTACT_EMAIL
fi
mapfile -t ALIASES < <(python3 -c 'import json,sys; print("\n".join(x["alias"] for x in json.load(open(sys.argv[1]))))' "$DATA")
if [[ "$MODE" == feature ]]; then
  # POST appends; posting an already-featured alias causes an error in 6.12.
  EXISTING="$(curl --fail-with-body --silent --show-error -H "X-Dataverse-key: $API_TOKEN" "$SERVER_URL/api/dataverses/$PARENT/featured")"
  PAYLOAD="$(python3 -c 'import json,sys; existing=json.loads(sys.argv[2])["data"]; print(json.dumps([x["alias"] for x in json.load(open(sys.argv[1])) if x["alias"] not in existing]))' "$DATA" "$EXISTING")"
  if [[ "$PAYLOAD" == '[]' ]]; then
    echo 'All example aliases are already featured.'
    exit 0
  fi
  curl --fail-with-body --silent --show-error -H "X-Dataverse-key: $API_TOKEN" \
    -H 'Content-Type: application/json' -X POST \
    --data-binary "$PAYLOAD" "$SERVER_URL/api/dataverses/$PARENT/featured"
  printf '\n'
  exit 0
fi
for ALIAS in "${ALIASES[@]}"; do
  printf '%s: %s\n' "$MODE" "$ALIAS"
  if [[ "$MODE" == create ]]; then
    python3 -c 'import json,os,sys; item=next(x for x in json.load(open(sys.argv[1])) if x["alias"]==sys.argv[2]); item["dataverseContacts"]=[{"contactEmail":os.environ["CONTACT_EMAIL"]}]; print(json.dumps(item,ensure_ascii=False))' "$DATA" "$ALIAS" | \
      curl --fail-with-body --silent --show-error -H "X-Dataverse-key: $API_TOKEN" \
        -H 'Content-Type: application/json' -X POST --data-binary @- \
        "$SERVER_URL/api/dataverses/$PARENT"
  else
    curl --fail-with-body --silent --show-error -H "X-Dataverse-key: $API_TOKEN" \
      -X POST "$SERVER_URL/api/dataverses/$ALIAS/actions/:publish"
  fi
  printf '\n'
done
