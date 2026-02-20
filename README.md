# Identity Service

Identity Service is a Spring Boot authentication service that provides JWT-based sign-up and sign-in endpoints backed by PostgreSQL. It is stateless, uses Flyway for migrations, and exposes OpenAPI docs via SpringDoc.

## Features

- JWT authentication (stateless, bearer token).
- User registration and sign-in with encrypted passwords.
- Role storage via many-to-many user/role mapping.
- Flyway migrations and Spring Boot Actuator.

## Technology Stack

- Spring Boot 4.0.1 (Java 21)
- Spring Security, Spring Data JPA
- PostgreSQL + Flyway
- JJWT for token creation/validation
- SpringDoc OpenAPI (Swagger UI)

## Prerequisites

- JDK 21+
- PostgreSQL 13+
- Gradle 9+ (or use `./gradlew`)

## Configuration

The application reads values from `src/main/resources/application.properties` and environment variables.

| Property | Environment Variable | Description |
| --- | --- | --- |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | JDBC URL (or compose via `POSTGRES_*` variables) |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | DB username |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | DB password |
| `jwt.secret` | `JWT_SECRET` | HMAC secret key (min 32 chars) |
| `jwt.expiration` | `JWT_EXPIRATION` | Token TTL in ms |
| `frontend.url` | `FRONTEND_URL` | Allowed CORS origin |

### Generate JWT Secret

```bash
openssl rand -base64 32
```

## Build & Run

```bash
./gradlew clean build -x test
./gradlew bootRun
```

Docker:

```bash
docker-compose up -d --build
```

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

### Authentication

- `POST /auth/signup` — register a new user, returns `{ "message": "User registered successfully" }`
- `POST /auth/signin` — authenticate and returns `{ "token": "<jwt>" }`

### JWT Notes

- Tokens store the user id (`UUID`) in the JWT `sub` claim.
- Supply JWT in `Authorization: Bearer <token>` for protected endpoints.

## Database Migrations

Flyway scripts live in `src/main/resources/db/migration` and run on startup.
