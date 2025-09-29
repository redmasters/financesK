# Stage 1: Builder
FROM eclipse-temurin:21.0.8_9-jdk-ubi9-minimal AS builder

# Install required packages for building (using microdnf for UBI)
RUN microdnf install -y findutils && microdnf clean all

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21.0.8_9-jre-ubi9-minimal AS app

# Create non-root user for security (using UBI commands)
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -m appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check (using curl instead of wget for UBI)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with optimized JVM settings for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
