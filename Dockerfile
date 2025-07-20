# Start with a Java runtime
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /appx`

# Copy built JAR (adjust filename)
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
