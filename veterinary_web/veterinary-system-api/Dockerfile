# ── Etapa 1: Compilación ─────────────────────────────────────────────────────
FROM maven:3.9.15-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

# ── Etapa 2: Imagen final ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.source="https://github.com/21Chambo/Proyecto-Arquitectura"

WORKDIR /app

RUN useradd -r -u 1001 -s /bin/false app

COPY --from=build /app/target/*.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 9090

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=8 \
    CMD curl -f http://localhost:9090/api/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-jar", "/app/app.jar"]
