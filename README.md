<div align="center">

# Identity Service

**Production-ready authentication & authorization microservice**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Gradle](https://img.shields.io/badge/Gradle-9+-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

[![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![JWT](https://img.shields.io/badge/JWT-HMAC--SHA256-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![OAuth2](https://img.shields.io/badge/OAuth2-OIDC-EB5424?style=flat-square&logo=auth0&logoColor=white)](https://oauth.net/2/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3.0-85EA2D?style=flat-square&logo=swagger&logoColor=black)](https://swagger.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

---

Issues **JWT tokens**, serves as an **OAuth2 Authorization Server**, and supports **OAuth2 Login** with third-party providers like Google — all backed by **PostgreSQL** and **Redis**.

[Getting Started](#quick-start) · [API Reference](#api-endpoints) · [Architecture](#architecture) · [Deployment](#deployment)

</div>

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Usage Examples](#usage-examples)
- [JWT Token Structure](#jwt-token-structure)
- [Email Verification Flow](#email-verification-flow)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure-clean-architecture)
- [Security](#security)
- [Configuration](#configuration)
- [Development](#development)
- [Deployment](#deployment)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| Feature | Description |
|:--------|:-----------|
| **JWT Authentication** | Stateless HMAC-SHA256 Bearer tokens |
| **OAuth2 Authorization Server** | Full OIDC support with RSA-256 signing |
| **OAuth2 Login** | Third-party authentication (Google and more) |
| **User Management** | Registration, authentication, email / username login |
| **Role-Based Access Control** | Multi-tier permissions — `USER`, `ADMIN`, `SUPER_ADMIN` |
| **Email Verification** | Token-based verification with Redis storage (30 min TTL) |
| **Resend Cooldown** | Rate-limited email resend (60 sec throttle) |
| **Clean Architecture** | Domain → Application → Infrastructure → Presentation |
| **Async Email** | Non-blocking sending via Spring `TaskExecutor` |
| **Automatic Migrations** | Flyway-managed schema versioning |
| **API Documentation** | Swagger UI with OpenAPI 3.0 |
| **CORS Support** | Configurable cross-origin access |

---

## Technology Stack

| Layer | Technology | Version |
|:------|:-----------|:--------|
| **Language** | Java (OpenJDK) | 21 |
| **Framework** | Spring Boot | 4.0.1 |
| **Security** | Spring Security + OAuth2 Authorization Server + OAuth2 Client | 6.x |
| **ORM** | Spring Data JPA + Hibernate | — |
| **Database** | PostgreSQL | 15+ |
| **Migrations** | Flyway | — |
| **Cache / Tokens** | Redis (Lettuce driver) | 7+ |
| **JWT** | jjwt (io.jsonwebtoken) | 0.12.3 |
| **API Docs** | SpringDoc OpenAPI | 3.0.0 |
| **Build** | Gradle | 9+ |
| **Container** | Docker & Docker Compose | — |

---

## Architecture

```
┌───────────────┐      ┌──────────────────────────────────────────────┐
│   Frontend    │─────▶│         Identity Service (Spring Boot)       │
│ (React, etc.) │      │                                              │
└───────────────┘      │  ① OAuth2 Authorization Server              │
                       │     /oauth2/**  /.well-known/**              │
┌───────────────┐      │     Issues OAuth2/OIDC tokens (RSA-256)     │
│ Microservices │─────▶│                                              │
│   (Backend)   │      │  ② JWT API — Stateless                      │
└───────────────┘      │     /auth/**  /api/**                        │
                       │     Bearer token validation (HMAC-SHA256)    │
┌───────────────┐      │                                              │
│ Google OAuth2 │◀────▶│  ③ OAuth2 Login + Form Login                │
└───────────────┘      │     Session-based fallback auth              │
                       └──────────────┬───────────────────────────────┘
                                      │
                          ┌───────────┴───────────┐
                          │                       │
                    ┌─────┴─────┐          ┌──────┴──────┐
                    │PostgreSQL │          │    Redis     │
                    │   15      │          │      7       │
                    │Users,Roles│          │Tokens,Cooldn │
                    └───────────┘          └─────────────┘
```

The service exposes **three security filter chains** ordered by priority:

1. **API chain** (`/auth/**`, `/api/**`, `/actuator/**`) — stateless JWT, no sessions
2. **Default chain** (everything else) — OAuth2 Login + Form Login with sessions
3. **Authorization Server** — built-in Spring Authorization Server endpoints

---

## Prerequisites

| Requirement | Minimum |
|:------------|:--------|
| JDK | 21+ |
| Docker & Docker Compose | latest |
| Gradle | 9+ _or_ use bundled `./gradlew` |
| PostgreSQL | 15+ _(or via Docker)_ |
| Redis | 7+ _(or via Docker)_ |

---

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/identity-service.git
cd identity-service
```

### 2. Create a `.env` file

```bash
cat > .env << 'EOF'
POSTGRES_DB=identity_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=secure_password

JWT_SECRET=generate-with-openssl-rand-base64-32
JWT_EXPIRATION=3600000

FRONTEND_URL=http://localhost:3000
BASE_URL=http://localhost:8080

REDIS_HOST=redis
REDIS_PORT=6379

SECURITY_USER=admin
SECURITY_PASSWORD=admin

GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
EOF
```

### 3. Start infrastructure only

```bash
docker compose up -d postgres redis
```

### 4. Run the application

```bash
./gradlew bootRun
```

### Or launch everything in Docker

```bash
docker compose up -d --build
```

> The service will be available at **http://localhost:8080**

---

## Environment Variables

| Variable | Required | Default | Description |
|:---------|:--------:|:-------:|:------------|
| `POSTGRES_DB` | ✅ | — | PostgreSQL database name |
| `POSTGRES_USER` | ✅ | — | PostgreSQL username |
| `POSTGRES_PASSWORD` | ✅ | — | PostgreSQL password |
| `JWT_SECRET` | ✅ | — | HMAC key for JWT signing (min 32 chars) |
| `JWT_EXPIRATION` | ✅ | — | Token lifetime in **milliseconds** (e.g. `3600000` = 1 h) |
| `FRONTEND_URL` | ✅ | — | CORS allowed origin |
| `BASE_URL` | ✅ | — | OAuth2 AS issuer URL |
| `REDIS_HOST` | — | `localhost` | Redis hostname |
| `REDIS_PORT` | — | `6379` | Redis port |
| `REDIS_PASSWORD` | — | _(empty)_ | Redis password |
| `SECURITY_USER` | — | — | Spring Security default user |
| `SECURITY_PASSWORD` | — | — | Spring Security default password |
| `GOOGLE_CLIENT_ID` | — | — | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | — | — | Google OAuth2 Client Secret |

---

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description | Body |
|:------:|:---------|:------------|:-----|
| `POST` | `/auth/register` | Register new user | `{ username, email, password }` |
| `POST` | `/auth/authenticate` | Login → JWT | `{ login, password }` |
| `POST` | `/auth/verify-email` | Verify email token | `{ token }` |
| `POST` | `/auth/resend-verification` | Resend verification | `{ email }` |

### Protected (Require `Authorization: Bearer <jwt>`)

| Method | Endpoint | Description |
|:------:|:---------|:------------|
| `*` | `/api/**` | Application-specific endpoints |

### OAuth2 Authorization Server

| Method | Endpoint | Description |
|:------:|:---------|:------------|
| `GET` | `/.well-known/openid-configuration` | OIDC discovery |
| `POST` | `/oauth2/token` | Token endpoint |
| `GET` | `/oauth2/authorize` | Authorization endpoint |
| `GET` | `/oauth2/jwks` | JSON Web Key Set |

### OAuth2 Social Login

| Method | Endpoint | Description |
|:------:|:---------|:------------|
| `GET` | `/oauth2/authorization/google` | Redirect to Google |

### Docs & Health

| Method | Endpoint | Description |
|:------:|:---------|:------------|
| `GET` | `/swagger-ui/index.html` | Swagger UI |
| `GET` | `/v3/api-docs` | OpenAPI 3.0 spec (JSON) |
| `GET` | `/actuator/health` | Health check |

---

## Usage Examples

<details>
<summary><b>Register a new user</b></summary>

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePassword123!"
  }'
```

```json
// 201 Created
{
  "message": "User registered successfully. Please check your email to verify your account."
}
```

</details>

<details>
<summary><b>Authenticate & get JWT</b></summary>

```bash
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "login": "john_doe",
    "password": "SecurePassword123!"
  }'
```

```json
// 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

</details>

<details>
<summary><b>Verify email</b></summary>

```bash
curl -X POST http://localhost:8080/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

```json
// 200 OK
{
  "message": "Email verified successfully",
  "verified": true
}
```

</details>

<details>
<summary><b>Resend verification email</b></summary>

```bash
curl -X POST http://localhost:8080/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com"
  }'
```

```json
// 200 OK
{
  "message": "Verification email sent successfully",
  "verified": false
}
```

</details>

<details>
<summary><b>Call a protected endpoint</b></summary>

```bash
curl http://localhost:8080/api/some-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

</details>

---

## JWT Token Structure

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ROLE_USER"],
  "iss": "Identity Service",
  "iat": 1740240000,
  "exp": 1740243600,
  "jti": "unique-token-identifier"
}
```

| Claim | Description |
|:------|:-----------|
| `sub` | User UUID |
| `roles` | Granted authorities |
| `iss` | Issuer identifier |
| `iat` | Issued-at (Unix epoch) |
| `exp` | Expiration (Unix epoch) |
| `jti` | Unique token ID |

---

## Email Verification Flow

```
 ┌──────────┐          ┌──────────────┐          ┌───────┐
 │  Client  │          │Identity Svc  │          │ Redis │
 └────┬─────┘          └──────┬───────┘          └───┬───┘
      │  POST /auth/register  │                      │
      │──────────────────────▶│  Create user          │
      │                       │  (PENDING_VERIFICATION)│
      │                       │  Generate UUID token   │
      │                       │──── SHA-256 hash ────▶│ SETEX (30 min)
      │                       │  Send email (async)    │
      │◁─── 201 Created ─────│                        │
      │                       │                        │
      │  POST /auth/verify    │                        │
      │──────────────────────▶│  Hash incoming token   │
      │                       │──── GET ─────────────▶│
      │                       │◁─── user_id ──────────│
      │                       │  Set ACTIVE            │
      │                       │──── DEL ─────────────▶│
      │◁─── 200 Verified ────│                        │
      │                       │                        │
      │  POST /auth/resend    │                        │
      │──────────────────────▶│  Check cooldown (60s)  │
      │                       │──── GET cooldown ────▶│
      │                       │  Generate new token    │
      │                       │──── SETEX ───────────▶│
      │                       │  Send email (async)    │
      │◁─── 200 Sent ────────│                        │
```

---

## Database Schema

### `users`

```sql
CREATE TABLE users (
    id                        UUID PRIMARY KEY,
    username                  VARCHAR(255) NOT NULL UNIQUE,
    email                     VARCHAR(255) NOT NULL UNIQUE,
    password                  VARCHAR(255) NOT NULL,
    permissions               VARCHAR(255),
    account_state             VARCHAR(50),
    security_status           VARCHAR(50),
    email_verified            BOOLEAN DEFAULT false,
    verified_at               TIMESTAMP,
    last_verification_sent_at TIMESTAMP,
    created_at                TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_account_state  ON users(account_state);
CREATE INDEX idx_users_email          ON users(email);
CREATE INDEX idx_users_username       ON users(username);
CREATE INDEX idx_users_email_verified ON users(email_verified);
```

### `roles`

```sql
CREATE TABLE roles (
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Seed data
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN'), ('ROLE_SUPER_ADMIN');
```

### `users_roles` (junction)

```sql
CREATE TABLE users_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
```

### ER Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    users     │       │ users_roles  │       │    roles     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id       (PK)│──┐    │ user_id (FK) │    ┌──│ id       (PK)│
│ username     │  └───▶│ role_id (FK) │◀───┘  │ name         │
│ email        │       └──────────────┘       └──────────────┘
│ password     │
│ account_state│  Enums: PENDING_VERIFICATION │ ACTIVE │ DISABLED │ DELETED
│ security_stat│  Enums: MFA_REQUIRED │ MFA_VERIFIED │ PENDING │ VERIFIED
│ email_verified│
│ created_at   │
└──────────────┘
```

---

## Project Structure (Clean Architecture)

```
src/main/java/dev/mathalama/identityservice/
│
├── domain/                              ← Business logic & rules
│   ├── entity/
│   │   ├── Users.java                     UserDetails aggregate root
│   │   └── Role.java                      GrantedAuthority entity
│   ├── enums/
│   │   ├── AccountState.java              PENDING_VERIFICATION · ACTIVE · DISABLED · DELETED
│   │   ├── SecurityStatus.java            MFA_REQUIRED · MFA_VERIFIED · PENDING · VERIFIED
│   │   └── KYCLifeCycle.java              KYC pipeline states
│   └── exception/
│       ├── UnauthorizedException.java
│       ├── UserAlreadyExistException.java
│       └── UserNotFoundException.java
│
├── application/                         ← Use cases & DTOs
│   ├── dto/
│   │   ├── SignUpRegister.java            Registration request
│   │   ├── SignInRequest.java             Login request
│   │   ├── UserResponse.java             User data response
│   │   ├── VerifyEmailRequest.java        Email verification request
│   │   ├── ResendVerificationRequest.java Resend request
│   │   ├── VerificationResponse.java      Verification status
│   │   ├── ErrorResponse.java             Error envelope
│   │   └── UpdateRequest.java             User update
│   └── service/
│       ├── AuthService.java               Interface — auth use cases
│       ├── EmailService.java              Interface — email sending
│       ├── JwtService.java                Interface — JWT ops
│       ├── VerificationTokenService.java  Interface — token mgmt
│       └── impl/
│           └── AuthServiceImpl.java       Core auth logic
│
├── infrastructure/                      ← Frameworks & drivers
│   ├── repository/
│   │   ├── UserRepository.java            Spring Data JPA
│   │   └── RoleRepository.java            Spring Data JPA
│   ├── service/
│   │   ├── EmailServiceImpl.java          Async email via TaskExecutor
│   │   ├── JwtServiceImpl.java            JWT generation & validation
│   │   └── VerificationTokenRedisService  Redis token storage
│   └── config/
│       ├── SecurityConfig.java            3 ordered filter chains
│       ├── JwtAuthenticationFilter.java   Bearer → SecurityContext
│       ├── AsyncConfig.java               Thread pool config
│       ├── RedisConfig.java               Redis connection & serialization
│       ├── PasswordConfig.java            BCryptPasswordEncoder
│       ├── WebConfig.java                 CORS configuration
│       └── FrontendProperties.java        @ConfigurationProperties
│
├── presentation/                        ← HTTP entry points
│   └── controller/
│       └── AuthController.java            /auth/** REST controller
│
└── IdentityServiceApplication.java      ← Spring Boot main class
```

---

## Security

### Password Handling

- **BCrypt** hashing with per-user salt
- Passwords are never logged or returned in responses

### JWT Token Security

- Signed with **HMAC-SHA256** (`JWT_SECRET`)
- Configurable expiration (default 1 hour)
- Payload: user UUID + roles — **no** sensitive data
- Validated on every request by `JwtAuthenticationFilter`

### Email Verification Tokens

- Stored in **Redis** (not the database)
- **SHA-256** hashed before storage — raw token only exists in the email link
- **30-minute** TTL, one-time use, deleted after verification
- Resend rate-limited to **60 seconds**

### Authentication Pipeline

```
HTTP Request
  └─▶ JwtAuthenticationFilter
        ├── Extract Bearer token from Authorization header
        ├── Validate HMAC-SHA256 signature
        ├── Assert token not expired
        ├── Parse user UUID + roles
        ├── Load User entity from DB
        └── Populate SecurityContext
              └─▶ @PreAuthorize / @Secured annotations enforce role checks
```

---

## Configuration

### Application Properties

Key configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${POSTGRES_DB}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}

app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}
  verification:
    token-expiry-minutes: 30
    resend-cooldown-seconds: 60
```

### Profiles

| Profile | Use |
|:--------|:----|
| _default_ | Local development (`localhost` DB & Redis) |
| _docker_ | Docker Compose network (`postgres`, `redis` hostnames) |

### Logging

```yaml
logging:
  level:
    dev.mathalama.identityservice: DEBUG
    org.springframework.security: INFO
```

---

## Development

```bash
# Build
./gradlew clean build

# Run tests
./gradlew test

# Start locally (requires running Postgres + Redis)
./gradlew bootRun

# Build Docker image
docker build -t mathalama/identity-service:latest .
```

---

## Deployment

### Docker Compose (recommended for dev / staging)

```bash
docker compose up -d --build
```

This spins up three containers:

| Container | Image | Port |
|:----------|:------|:-----|
| `identity-service` | Custom build | `8080` |
| `identity-postgres` | `postgres:15-alpine` | `5432` |
| `identity-redis` | `redis:7-alpine` | `6379` |

### Production Checklist

```bash
# Generate a strong JWT secret
openssl rand -base64 32

# Required env vars
JWT_SECRET=<generated_above>
JWT_EXPIRATION=3600000
FRONTEND_URL=https://yourdomain.com
BASE_URL=https://api.yourdomain.com
POSTGRES_PASSWORD=<strong_password>
REDIS_PASSWORD=<strong_password>
```

- [ ] Use external managed PostgreSQL & Redis
- [ ] Enable TLS termination (reverse proxy / load balancer)
- [ ] Rotate `JWT_SECRET` periodically
- [ ] Set `spring.jpa.show-sql=false`
- [ ] Enable Prometheus metrics scraping (`/actuator/prometheus`)

---

## Monitoring & Health Checks

```bash
curl http://localhost:8080/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db":    { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

| Endpoint | Purpose |
|:---------|:--------|
| `/actuator/health` | Liveness & readiness |
| `/actuator/prometheus` | Prometheus metrics |

---

## Troubleshooting

| Problem | Solution |
|:--------|:--------|
| _JWT claims string is empty_ | Ensure header is `Authorization: Bearer <token>` (note the space) |
| _User not found_ | Verify user exists and email is verified (`account_state = ACTIVE`) |
| _Verification email was recently sent_ | Wait 60 seconds before resending |
| _Invalid token_ | Token expired (30 min window) or already used |
| _Redis connection failed_ | Check `REDIS_HOST` / `REDIS_PORT` and that Redis is running |

---

## Contributing

1. **Fork** the repository
2. Create a feature branch — `git checkout -b feature/amazing-feature`
3. Follow **Clean Architecture** layer boundaries
4. Add tests for new functionality
5. Commit — `git commit -m 'Add amazing feature'`
6. Push — `git push origin feature/amazing-feature`
7. Open a **Pull Request**

---

## License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

<div align="center">

**Built with Spring Boot 4 & Java 21**

</div>
