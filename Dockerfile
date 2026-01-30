# Stage 1: Build
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN gradle clean jar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create directory for database and logs
RUN mkdir -p /app/data /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Environment variables
ENV SERVER_PORT=7070
ENV DATABASE_URL=jdbc:sqlite:/app/data/bookmarks.db

# Expose port
EXPOSE 7070

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:7070/ || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
