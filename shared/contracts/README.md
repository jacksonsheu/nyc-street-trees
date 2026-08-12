# Shared contracts

`openapi.json` is the **generated** API contract — the single source of truth
for request/response shapes shared by the web and mobile clients. It is
produced from the backend's Spring annotations/DTOs, not hand-written.

## Regenerating after a backend change

```bash
# 1. Start the backend (from backend/)
./gradlew bootRun &

# 2. Regenerate the spec (from repo root)
./scripts/generate-openapi-spec.sh

# 3. Regenerate client types (from web/)
cd web && npm run generate:types
```

This updates `web/src/types/api.generated.ts`, which `web/src/types/tree.ts`
derives its domain types from. Do not hand-edit `openapi.json` or
`api.generated.ts` — both are build artifacts.
