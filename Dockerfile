# Stage 1 - Build
FROM gradle:jdk21-alpine AS build

ENV APP_HOME=/app
WORKDIR $APP_HOME

COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew $APP_HOME/
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew clean build --no-daemon -x test

# Stage 2 - Runtime
FROM eclipse-temurin:21-jre-alpine

# Configurar timezone
RUN apk add --no-cache tzdata
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=build /app/build/libs/financesK-*.jar /app/app.jar

# Configurar usuário não-root
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p /app && \
    chown -R appuser:appgroup /app

WORKDIR /app

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
