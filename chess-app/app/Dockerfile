# Use a lightweight JDK 17 image
FROM eclipse-temurin:21-jdk-alpine

# Set metadata (optional)
LABEL maintainer="gourav200056@gmail.com"

# Set working directory in container
WORKDIR /app

# Copy the built jar file into the image
COPY build/libs/app-0.0.1-SNAPSHOT.jar app.jar

# Expose backend service port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]