# ── Etapa 1: Compilación ─────────────────────────────────────────────────────
# Node 18 LTS soportado; --openssl-legacy-provider lo requiere Angular 10 / webpack 4.
FROM node:18-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ── Etapa 2: Imagen final ────────────────────────────────────────────────────
FROM nginxinc/nginx-unprivileged:1.27-alpine

USER root
RUN apk upgrade --no-cache
USER 101

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend-unab /usr/share/nginx/html

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/ || exit 1
