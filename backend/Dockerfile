# ============================================
# Dockerfile para GraalVM Native Image
# ============================================

# === Etapa 1: Build con GraalVM Native Image ===
FROM ghcr.io/graalvm/native-image-community:25 AS builder

WORKDIR /app

# Cachear dependencias (esta capa se reutiliza si no cambia pom.xml)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve -Pnative -q

# Copiar código fuente y compilar como imagen nativa
COPY src/ src/
RUN ./mvnw -Pnative native:compile -DskipTests -q

# === Etapa 2: Imagen mínima de ejecución (NO necesita JVM) ===
FROM debian:bookworm-slim

WORKDIR /app

# Solo libstdc++ es necesario para el binario nativo
RUN apt-get update && \
    apt-get install -y --no-install-recommends libstdc++6 ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Copiar el binario nativo compilado
COPY --from=builder /app/target/backend ./backend
RUN chmod +x ./backend

EXPOSE 8080

ENTRYPOINT ["./backend"]
