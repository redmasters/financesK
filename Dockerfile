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

# Stage 1 - Build (Alpine base)
FROM gradle:jdk21-alpine AS build

ENV APP_HOME=/app
WORKDIR $APP_HOME

COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew $APP_HOME/
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew clean build --no-daemon -x test

# Stage 2 - Runtime (Alpine minimal)
FROM eclipse-temurin:21-jre-alpine

ENV APP_HOME=/app

# Copiar e importar certificado como root
COPY --from=build /app/build/libs/financesK-*.jar /tmp/app.jar
COPY /home/ubuntu/.postgresql/root.crt /tmp/root.crt

RUN export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java)))) && \
    keytool -importcert -noprompt \
        -alias supabase-root \
        -file /tmp/root.crt \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit && \
    rm /tmp/root.crt

# Configurar usuário não-root
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p $APP_HOME && \
    mv /tmp/app.jar $APP_HOME/app.jar && \
    chown -R appuser:appgroup $APP_HOME

WORKDIR $APP_HOME
USER appuser

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
