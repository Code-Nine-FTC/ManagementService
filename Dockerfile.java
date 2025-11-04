# ===== STAGE 1: Build =====
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação (pular testes para build mais rápido)
RUN mvn clean package -DskipTests -B

# ===== STAGE 2: Runtime =====

FROM eclipse-temurin:21-jre

WORKDIR /app

# Instalar curl para health check
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring

# Copiar JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Mudar ownership
RUN chown -R spring:spring /app

USER spring

# Expor porta
EXPOSE 8080

# Configurações JVM otimizadas para Java 21
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Executar aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
