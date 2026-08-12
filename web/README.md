# Web app

# Web app

React + TypeScript + Vite client for browsing nearby NYC street trees on a map and
logging maintenance requests / waterings against them.

## Stack

- **Vite + React + TypeScript** — fast dev server, typed components
- **React Leaflet** (OpenStreetMap tiles) — no API key required, good default for a POC
- **TanStack Query** — server-state caching, retries, and mutation handling

## Getting started

```bash
npm install
cp .env.example .env   # point at your backend, defaults to http://localhost:8080/api
npm run dev
```

Requires the backend (`cd ../backend && ./gradlew bootRun`) running on the URL configured
in `.env`.

## Structure

```text
src/
├── api/          # fetch wrapper + typed calls to the backend REST API
├── components/   # MapView, TreeDetailPanel, interaction forms, toasts
├── hooks/        # geolocation + React Query hooks for trees/interactions
├── types/        # domain types derived from the generated OpenAPI contract
└── styles/       # global CSS (design tokens + layout)
```

## API contract

`src/types/api.generated.ts` is generated from `../shared/contracts/openapi.json`
(itself generated from the backend) — see `../shared/contracts/README.md`.
`src/types/tree.ts` re-exports/narrows those generated types rather than
hand-duplicating backend DTOs, so the frontend can't silently drift out of
sync with the API. Regenerate after a backend change:

```bash
# with the backend running (./gradlew bootRun)
../scripts/generate-openapi-spec.sh   # from repo root
npm run generate:types                 # from web/
```

## Features

- Geolocates the user (falling back to a Manhattan default) and loads nearby trees
- "Search this area" appears after panning so map moves don't trigger unbounded fetches
- Click a tree marker to view details (species, health, DBH, address, etc.)
- Log a watering event or submit a maintenance request per tree
- Activity history per tree, with toast confirmations
- Responsive layout: side panel on desktop, bottom sheet on mobile

## Scripts

- `npm run dev` — start the dev server
- `npm run build` — typecheck + production build
- `npm run lint` — ESLint
- `npm run typecheck` — TypeScript only

