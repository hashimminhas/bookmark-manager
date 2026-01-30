# Multi-stage Dockerfile for Bookmark Manager
# Stage 1: Build the application
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle gradle
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user and data directory
RUN groupadd -r appgroup && useradd -r -g appgroup appuser && \
    mkdir -p /app/data && \
    chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Environment variables with defaults
ENV PORT=8888
ENV DB_URL=jdbc:sqlite:/app/data/bookmarks.db

# Expose the port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${PORT}/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
