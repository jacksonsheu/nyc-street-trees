#!/usr/bin/env bash
# Regenerates the OpenAPI contract from the running backend and saves it to
# shared/contracts/. Run this after changing backend DTOs/controllers so
# clients can regenerate their types from a single source of truth instead
# of hand-editing duplicated type definitions.
#
# Usage:
#   ./gradlew bootRun &        # from backend/, in another terminal/tab
#   ./scripts/generate-openapi-spec.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
OUTPUT_FILE="$REPO_ROOT/shared/contracts/openapi.json"

echo "Fetching OpenAPI spec from $BACKEND_URL/v3/api-docs ..."
if ! curl -sf "$BACKEND_URL/v3/api-docs" -o "$OUTPUT_FILE.tmp"; then
  echo "error: could not reach $BACKEND_URL — is the backend running (./gradlew bootRun)?" >&2
  rm -f "$OUTPUT_FILE.tmp"
  exit 1
fi

python3 -m json.tool "$OUTPUT_FILE.tmp" > "$OUTPUT_FILE"
rm -f "$OUTPUT_FILE.tmp"

echo "Wrote $OUTPUT_FILE"
echo "Next: run 'npm run generate:types' in web/ to regenerate TypeScript types from this spec."
