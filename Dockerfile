# ── Build ────────────────────────────────────────
# Etapa 1: compilar con Maven sobre JDK 21
FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ── Runtime ──────────────────────────────────────
# Etapa 2: imagen ligera solo con JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/target/app.jar app.jar

EXPOSE 8080

USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
