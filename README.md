# Identity Service

Identity Service is a robust and scalable authentication and user management system built with Spring Boot. It provides secure endpoints for user registration, authentication, and role-based access control using JWT tokens. Designed for high performance and reliability with a microservices architecture.

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/java-21-blue)
![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.1-green)
![Auth](https://img.shields.io/badge/auth-JWT-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Quick Start](#quick-start)
- [Migration Info](#migration-info)

## Features

- **JWT Authentication**: Secure stateless authentication using JSON Web Tokens.
- **User Management**: Endpoints for registering users, changing passwords, and managing profiles.
- **Microservices Ready**: Stateless architecture for horizontal scalability without session storage.
- **Role-Based Access**: Support for role-based access control (RBAC).
- **API Documentation**: Interactive API documentation via Swagger UI (OpenAPI 3).
- **Database Migrations**: Version-controlled schema changes with Flyway.
- **Health Checks**: Application health and metrics via Spring Boot Actuator.

## Technology Stack

- **Core Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens) with JJWT library
- **Security**: Spring Security
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Gradle

## Prerequisites

Ensure the following software is installed:

- Java Development Kit (JDK) 21 or higher
- PostgreSQL 13 or higher
- Gradle 9.0 or higher (or use included gradlew)

## Configuration

The application is configured using `src/main/resources/application.properties`. For production, override with environment variables.

### JWT Configuration

| Property | Environment Variable | Description | Default |
| :--- | :--- | :--- | :--- |
| `jwt.secret` | `JWT_SECRET` | Secret key for signing tokens (min 32 chars) | `your-256-bit-secret-...` |
| `jwt.expiration` | `JWT_EXPIRATION` | Token expiration time in milliseconds | `3600000` (1 hour) |

### Database Configuration

| Property | Environment Variable | Description |
| :--- | :--- | :--- |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | Database username |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Database password |

### ⚠️ Production Security

Before deploying to production, generate a secure JWT secret:

```bash
openssl rand -base64 32
```

Then set it as an environment variable:

```bash
export JWT_SECRET="your-generated-key-here"
```

## Building the Application

To build the application (without tests):

```bash
./gradlew clean build -x test
```

This produces an executable JAR in `build/libs/Identity-Service-0.0.1-SNAPSHOT.jar`

## Running the Application

### Local Development

Ensure PostgreSQL is running, then:

```bash
./gradlew bootRun
```

### Production with Environment Variables

```bash
export JWT_SECRET="your-secure-key"
export JWT_EXPIRATION=3600000
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/identity_service_db"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"

java -jar build/libs/Identity-Service-0.0.1-SNAPSHOT.jar
```

### Docker Support

To run using Docker Compose:

```bash
docker-compose up -d --build
```

The application will be available at `http://localhost:8080`.

## API Documentation

The application exposes interactive API documentation via Swagger UI when running.

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Key Endpoints (JWT-based)

**Authentication:**
- `POST /auth/signup`: Register a new user
- `POST /auth/login`: Authenticate and receive JWT token
- `GET /auth/me`: Get current user (requires token)
- `POST /auth/change-password`: Change password (requires token)

**User Management (Admin Only):**
- `GET /user/all`: List all users
- `POST /user/addUser`: Create a new user
- `DELETE /user/{username}`: Delete a user
- `POST /user/{username}/role`: Assign a role

### Authentication Header

All protected endpoints require the JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

Example:
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." http://localhost:8080/auth/me
```

## Quick Start

For a quick start guide with examples, see [QUICK_START.md](QUICK_START.md)

## Migration from Sessions to JWT

This project has been migrated from session-based authentication (Redis) to stateless JWT tokens for microservices architecture. See [JWT_MIGRATION.md](JWT_MIGRATION.md) for detailed migration information and [JWT_USAGE_EXAMPLES.md](JWT_USAGE_EXAMPLES.md) for code examples.

## Database Migrations

Database schema changes are managed by Flyway and applied automatically on startup.

- **Location**: `src/main/resources/db/migration`
- **Validation**: The application validates applied migrations

## Monitoring

Spring Boot Actuator is enabled for operational monitoring.

- **Health Check**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`

---

&copy; 2026 Identity Service Project. All rights reserved.
