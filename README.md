# NYC Street Trees Monorepo

Monorepo for a Spring Boot backend with web and mobile clients, sharing a
single generated API contract.

## Structure

```text
.
├── backend/         # Spring Boot API
├── web/              # React web app
├── mobile/           # React Native / Expo mobile app (next step)
├── shared/contracts/ # Generated OpenAPI spec — the single source of truth
│                      # for request/response shapes across clients
└── scripts/          # Repo-wide dev scripts (contract regeneration, etc.)
```

## Getting started

1. Backend:
   - `cd backend`
   - `./gradlew bootRun`
2. Web:
   - `cd web`
   - `npm install`
   - `npm run dev`
3. Mobile:
   - `cd mobile`
   - `npm install`
   - `npm run start`

## API contract

The backend's DTOs/controllers are the source of truth. `shared/contracts/openapi.json`
and the web app's `src/types/api.generated.ts` are both generated artifacts —
regenerate them after changing a backend DTO or endpoint:

```bash
# with the backend running (./gradlew bootRun)
./scripts/generate-openapi-spec.sh
cd web && npm run generate:types
```

See `shared/contracts/README.md` for details.
