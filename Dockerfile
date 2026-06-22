# =============================================================================
# Multi-stage Dockerfile for Expense Tracker API
# Stage 1: Build with Maven
# Stage 2: Minimal JRE runtime
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn/

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="admin@expensetracker.com"
LABEL description="Expense Tracker API"
LABEL version="1.0"

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup app.jar
USER appuser

# Expose application port
EXPOSE 8080

# JVM optimization flags
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
