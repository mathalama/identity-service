# Build Stage
FROM gradle:8.14 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8010
ENTRYPOINT ["java", "-jar", "app.jar"]