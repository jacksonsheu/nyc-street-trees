# NYC Street Trees

Web app built on NYC's public dataset for street trees, featuring an interactive map that allows users to view details of nearby trees and interact with them by watering or submitting maintenance requests. Spring Boot/Java backend with React web client. 

Live website: https://nyc-trees.onrender.com/

<img width="1094" height="674" alt="image" src="https://github.com/user-attachments/assets/32778e44-c849-4998-baa5-d34edf7d7dcc" />


## Structure

```text
.
├── backend/         # Spring Boot API
├── web/              # React web app
├── mobile/           # future mobile app
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

