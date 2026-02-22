# Identity Service

Микросервис аутентификации и авторизации на Spring Boot. Выдаёт JWT токены, поддерживает OAuth2 Authorization Server и OAuth2 Login (Google).

## Features

- **JWT аутентификация** — stateless, HMAC-SHA256, bearer token
- **OAuth2 Authorization Server** — выдаёт OAuth2/OIDC токены другим сервисам (RSA-256)
- **OAuth2 Client** — логин через Google
- **Регистрация и вход** — email или username, BCrypt хеширование паролей
- **Ролевая модель** — `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN` (many-to-many)
- **Flyway миграции** — автоматические при старте
- **CORS** — настраиваемый origin через `FRONTEND_URL`
- **SpringDoc OpenAPI** — Swagger UI из коробки

## Technology Stack

| Компонент | Технология |
|-----------|-----------|
| Язык | Java 21 |
| Фреймворк | Spring Boot 4.0.1 |
| Безопасность | Spring Security + OAuth2 Authorization Server + OAuth2 Client |
| ORM | Spring Data JPA + Hibernate |
| БД | PostgreSQL 15 |
| Миграции | Flyway |
| JWT | jjwt 0.12.3 (для API токенов) |
| API Docs | SpringDoc OpenAPI 3.0.0 |
| Кэш | Redis 7 (подготовлен) |
| Контейнеры | Docker Compose |

## Architecture

```
┌──────────────┐     ┌──────────────────────────────────────┐
│   Frontend   │────▶│         Identity Service              │
│  (React, etc)│     │                                      │
└──────────────┘     │  SecurityFilterChain Order(1):       │
                     │    OAuth2 Authorization Server       │
┌──────────────┐     │    /oauth2/**, /.well-known/**       │
│ Other        │     │                                      │
│ Microservices│────▶│  SecurityFilterChain Order(2):       │
│              │     │    JWT API (stateless)               │
└──────────────┘     │    /auth/**, /api/**                 │
                     │                                      │
┌──────────────┐     │  SecurityFilterChain Order(3):       │
│   Google     │◀───▶│    OAuth2 Login + Form Login         │
│   OAuth2     │     │                                      │
└──────────────┘     └──────────┬───────────────────────────┘
                               │
                     ┌─────────┴─────────┐
                     │   PostgreSQL 15    │
                     │   Redis 7         │
                     └───────────────────┘
```

## Prerequisites

- JDK 21+
- Docker & Docker Compose
- Gradle 9+ (или используй `./gradlew`)

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_DB` | Имя базы данных | `identity_db` |
| `POSTGRES_USER` | Пользователь БД | `postgres` |
| `POSTGRES_PASSWORD` | Пароль БД | `password` |
| `JWT_SECRET` | HMAC ключ для подписи JWT (мин. 32 символа) | `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Время жизни JWT токена (мс) | `3600000` (1 час) |
| `FRONTEND_URL` | Разрешённый CORS origin | `http://localhost:3000` |
| `SECURITY_USER` | Spring Security default user | `admin` |
| `SECURITY_PASSWORD` | Spring Security default password | `admin` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | `xxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret | `yyy` |
| `BASE_URL` | OAuth2 Authorization Server issuer | `http://localhost:8080` |

## Quick Start

### 1. Создай `.env` файл

```bash
cat > .env << 'EOF'
POSTGRES_DB=identity_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password
JWT_SECRET=my-super-secret-key-that-is-at-least-32-bytes
JWT_EXPIRATION=3600000
SECURITY_USER=admin
SECURITY_PASSWORD=admin
FRONTEND_URL=http://localhost:3000
BASE_URL=http://localhost:8080
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
EOF
```

### 2. Запусти инфраструктуру

```bash
docker compose up -d postgres redis
```

### 3. Запусти приложение

```bash
./gradlew bootRun
```

### 4. Или всё через Docker

```bash
docker compose up -d --build
```

## API Endpoints

### Public (без авторизации)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Регистрация нового пользователя |
| `POST` | `/auth/authenticate` | Аутентификация, возвращает JWT |
| `GET` | `/actuator/health` | Health check |

### Protected (требуется JWT)

| Method | Path | Description |
|--------|------|-------------|
| `*` | `/api/**` | Защищённые API эндпоинты (в разработке) |

### OAuth2 Authorization Server

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/.well-known/openid-configuration` | OIDC Discovery |
| `POST` | `/oauth2/token` | Выдача OAuth2 токенов |
| `GET` | `/oauth2/authorize` | OAuth2 Authorization |
| `GET` | `/oauth2/jwks` | JSON Web Key Set |

### OAuth2 Login

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/oauth2/authorization/google` | Логин через Google |

## Usage Examples

### Регистрация

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"mypassword"}'

# Response: 201 Created
# { "message": "User registered successfully" }
```

### Аутентификация

```bash
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"login":"john","password":"mypassword"}'

# Response: 200 OK
# { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

### Запрос с JWT

```bash
curl http://localhost:8080/api/some-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## JWT Token Structure

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ROLE_USER"],
  "iss": "Identity Service",
  "iat": 1740240000,
  "exp": 1740243600,
  "jti": "unique-token-id"
}
```

## API Documentation

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Database

Flyway миграции в `src/main/resources/db/migration/`:

| Миграция | Описание |
|----------|----------|
| V1 | Создание таблицы `users` |
| V2 | Создание таблицы `roles`, связь `users_roles`, начальные роли |
| V3 | Зарезервирована |

### Схема

```
users                    roles                users_roles
├── id (UUID PK)         ├── id (UUID PK)     ├── user_id (FK → users)
├── username (UNIQUE)    └── name (UNIQUE)    └── role_id (FK → roles)
├── email (UNIQUE)
├── password
├── permissions
├── account_state
├── security_status
└── created_at
```

## Project Structure

```
src/main/java/dev/mathalama/identityservice/
├── config/
│   ├── SecurityConfig.java          # 3 SecurityFilterChains
│   ├── JwtAuthenticationFilter.java # JWT → SecurityContext
│   ├── PasswordConfig.java          # BCryptPasswordEncoder
│   ├── WebConfig.java               # CORS
│   └── FrontendProperties.java      # Frontend URL config
├── controller/
│   └── AuthController.java          # /auth endpoints
├── service/
│   ├── AuthService.java             # Register, authenticate, password
│   ├── JwtService.java              # Generate, validate, parse JWT
│   └── UserService.java             # User CRUD operations
├── entity/
│   ├── Users.java                   # JPA entity + UserDetails
│   └── Role.java                    # JPA entity + GrantedAuthority
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── dto/
│   ├── SignInRequest.java
│   ├── SignUpRegister.java
│   ├── UserResponse.java
│   ├── ErrorResponse.java
│   └── enums/
│       ├── AccountState.java
│       └── SecurityStatus.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── UnauthorizedException.java
    ├── UserAlreadyExistException.java
    └── UserNotFoundException.java
```
