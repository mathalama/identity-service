# Identity Service

Identity Service is a robust and scalable authentication and user management system built with Spring Boot. It provides secure endpoints for user registration, authentication, session management, and role-based access control. Designed for high performance and reliability, this service leverages modern Java standards and industry-best practices for security and data integrity.

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/java-17-blue)
![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.1-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Migrations](#database-migrations)
- [Monitoring](#monitoring)

## Features

- **User Authentication**: Secure login and logout functionality using Spring Security.
- **Session Management**: Distributed session storage using Redis and Spring Session for horizontal scalability.
- **User Management**: Endpoints for registering users, resetting passwords, and managing user profiles.
- **API Documentation**: Interactive API documentation automatically generated via Swagger UI (OpenAPI 3).
- **Database Migrations**: Version-controlled database schema changes managed by Flyway.
- **Health Checks**: Application health and metrics monitoring via Spring Boot Actuator.

## Technology Stack

- **Core Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Database**: PostgreSQL
- **Caching & Session**: Redis (Spring Session Data Redis)
- **Security**: Spring Security
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Gradle

## Prerequisites

Ensure the following software is installed on your deployment environment:

- Java Development Kit (JDK) 17 or higher
- PostgreSQL 13 or higher
- Redis 6 or higher

## Configuration

The application is configured using `src/main/resources/application.properties`. For production environments, it is recommended to override these settings using environment variables.

| Property | Environment Variable | Description |
| :--- | :--- | :--- |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | Database username |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Database password |
| `spring.data.redis.host` | `SPRING_DATA_REDIS_HOST` | Redis server hostname |
| `spring.data.redis.port` | `SPRING_DATA_REDIS_PORT` | Redis server port |
| `spring.data.redis.password` | `SPRING_DATA_REDIS_PASSWORD` | Redis password (if applicable) |

## Building the Application

To build the application and run tests, use the included Gradle wrapper:

```bash
./gradlew clean build
```

This command produces an executable JAR file in the `build/libs` directory.

## Running the Application

### Local Development

Ensure PostgreSQL and Redis are running locally on their default ports (or update `application.properties`), then run:

```bash
./gradlew bootRun
```

### Production

To run the application in a production environment using the built JAR file:

```bash
java -jar build/libs/identity-service-0.0.1-SNAPSHOT.jar
```

Ensure that the required environment variables for the database and Redis connections are set before executing the command.

## API Documentation

The application exposes interactive API documentation accessible via a web browser when the service is running.

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Database Migrations

Database schema changes are handled automatically by Flyway on application startup.

- **Location**: Migration scripts are located in `src/main/resources/db/migration`.
- **Validation**: The application is configured to validate the applied migrations against the available classpath scripts to ensure integrity (`spring.jpa.hibernate.ddl-auto=validate`).

## Monitoring

Spring Boot Actuator is enabled to provide operational information about the running application.

- **Health Check**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`

---

&copy; 2026 Identity Service Project. All rights reserved.
