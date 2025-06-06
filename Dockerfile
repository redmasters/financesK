# Stage 1 - Build (Alpine base)
FROM gradle:jdk21-alpine AS build
LABEL maintainer="redmaster"

ENV APP_HOME=/app
WORKDIR $APP_HOME

# 1. Copiar apenas arquivos necessários para o cache de dependências
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew $APP_HOME/

# 2. Resolver dependências primeiro (cache layer)
RUN ./gradlew dependencies --no-daemon

# 3. Copiar código fonte
COPY src src

# 4. Build com cache otimizado
RUN ./gradlew clean build --no-daemon -x test

# Stage 2 - Runtime (Alpine minimal)
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="redmaster"

ENV APP_HOME=/app \
    USER=appuser \
    GROUP=appgroup

# 5. Cria usuário não-root seguro
RUN addgroup -S $GROUP && \
    adduser -S $USER -G $GROUP && \
    mkdir -p $APP_HOME && \
    mkdir -p /etc/postgresql-ssl && \
    mkdir -p /etc/letsencrypt/live/financesk.ddns.net && \
    # Ajusta permissões
    chown -R $USER:$GROUP $APP_HOME && \
    chown 755 /etc/postgresql-ssl && \
    chown 755 /etc/letsencrypt/live/financesk.ddns.net

WORKDIR $APP_HOME

# 6. Copia apenas o JAR necessário
COPY --from=build --chown=$USER:$GROUP $APP_HOME/build/libs/financesK-*.jar app.jar

# 7. Healthcheck sem dependências externas (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 8. Execução segura
USER $USER

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
