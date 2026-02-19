# Build Stage
# Updated from 8.5 to 8.14 to satisfy Spring Boot 4.0.1 requirements
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Using a wildcard in COPY can sometimes fail if there are multiple jars (like plain.jar)
# Ensure your build produces only one executable jar or specify the name
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]