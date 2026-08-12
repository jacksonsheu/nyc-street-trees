# Builds a single deployable image containing the Spring Boot API with the
# React web app's production build embedded as static resources, so Render
# only needs to run one service for the whole app.

# --- Stage 1: build the React web app -------------------------------------
FROM node:22-bookworm-slim AS frontend-builder
WORKDIR /app/web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
# .env.production (VITE_API_BASE_URL=/api) is picked up automatically by
# `vite build`, so the built app calls the API on its own origin.
RUN npm run build

# --- Stage 2: build the Spring Boot jar, embedding the frontend build ------
FROM eclipse-temurin:26-jdk-jammy AS backend-builder
WORKDIR /app/backend
COPY backend/ ./
COPY --from=frontend-builder /app/web/dist ./src/main/resources/static
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# --- Stage 3: minimal runtime image ----------------------------------------
FROM eclipse-temurin:26-jre-jammy
WORKDIR /app
COPY --from=backend-builder /app/backend/build/libs/app.jar app.jar
# Render sets $PORT at runtime; application.properties binds server.port to it.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
