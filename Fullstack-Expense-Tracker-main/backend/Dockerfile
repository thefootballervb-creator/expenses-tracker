# Multi-stage build for Spring Boot application
# Note: Build context is repository root, so we need to copy from backend/ prefix

# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
# Build context is repo root, so copy from backend/ folder
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget for health check
RUN apk add --no-cache wget

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory before switching user
RUN mkdir -p /app/uploads/user/profile && chown -R spring:spring /app

USER spring:spring

# Copy the JAR file from build stage
COPY --from=build /app/target/expenseTracker-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/mypockit/category/getAll || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
