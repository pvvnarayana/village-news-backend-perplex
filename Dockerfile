# ------------ STAGE 1: Build with Gradle ------------
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# Copy all files
COPY --chown=gradle:gradle . .

# Build the project (skip tests for faster CI builds)
RUN gradle build -x test

# ------------ STAGE 2: Run the JAR ------------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
