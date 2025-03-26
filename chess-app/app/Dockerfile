# Use a lightweight JDK 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set metadata (optional)
LABEL maintainer="gourav200056@gmail.com"

# Set working directory in container
WORKDIR /app

# Copy the built jar file into the image
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Expose backend service port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]