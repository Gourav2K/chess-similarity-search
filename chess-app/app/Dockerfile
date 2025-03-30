# ----- Stage 1: Build -----
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Copy everything and build
COPY . .
RUN gradle clean build -x test --no-daemon

# ----- Stage 2: Runtime -----
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/build/libs/app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]