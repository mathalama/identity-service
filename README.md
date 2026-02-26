# Identity Service

Authentication and authorization microservice built with Spring Boot. Issues JWT tokens, supports OAuth2 Authorization Server, and OAuth2 Login integration.

![Java](https://img.shields.io/badge/Java-21-red)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![JWT](https://img.shields.io/badge/JWT-HMAC_SHA256-yellow)
![OAuth2](https://img.shields.io/badge/OAuth2-OIDC-blue)
![Gradle](https://img.shields.io/badge/Gradle-9+-lightgrey)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Features

- **JWT Authentication** - Stateless HMAC-SHA256 with Bearer tokens
- **OAuth2 Authorization Server** - Full OIDC support with RSA-256 signing
- **OAuth2 Login** - Third-party authentication (Google and more)
- **User Management** - Registration, authentication, email/username login
- **Role-Based Access** - Multi-tier permissions (USER, ADMIN, SUPER_ADMIN)
- **Email Verification** - Token-based verification with Redis storage (30 min TTL)
- **Resend Cooldown** - Rate-limited email resend (60 sec throttle)
- **Clean Architecture** - Domain, Application, Infrastructure, Presentation layers
- **Async Email** - Non-blocking email sending via TaskExecutor
- **Automatic Migrations** - Flyway-based schema management
- **API Documentation** - Swagger UI with OpenAPI 3.0
- **CORS Support** - Configurable cross-origin access

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.1 |
| Security | Spring Security 6.x + OAuth2 Authorization Server + OAuth2 Client |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15+ |
| Migrations | Flyway |
| Token Storage | Redis 7+ (verification tokens, cooldowns) |
| JWT Library | jjwt 0.12.3 (HMAC-SHA256 signing) |
| API Docs | SpringDoc OpenAPI 3.0.0 |
| Async | Spring Task Executor (email sending) |
| Build Tool | Gradle 9+ |
| Containerization | Docker & Docker Compose |

## Architecture

```
+---------------+      +----------------------------------------------+
| Frontend      |····> | Identity Service (Spring Boot)            |
| (React, etc.) |      |                                            |
+---------------+      | [1] OAuth2 Authorization Server           |
                       |     /oauth2/**, /.well-known/**           |
+---------------+      |     (issues OAuth2/OIDC tokens)          |
| Microservices |····> |                                            |
| (Backend)     |      | [2] JWT API - Stateless                  |
+---------------+      |     /auth/**, /api/**                    |
                       |     (Bearer token validation)             |
+---------------+      |                                            |
| Google OAuth2 | <··· | [3] OAuth2 Login + Form Login            |
+---------------+      | (session-based, fallback auth)           |
                       |                                            |
                       +------+----------------------------------+
                              |
                    +----------+----------+
                    |                     |
              PostgreSQL 15          Redis 7
             (Users, Roles)     (Tokens, Cooldowns)
```

## Prerequisites

- JDK 21 or higher
- Docker & Docker Compose
- Gradle 9+ (or use bundled `./gradlew`)
- PostgreSQL 15+ (or use Docker)
- Redis 7+ (for token storage, or use Docker)

## Environment Variables

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `POSTGRES_DB` | Yes | PostgreSQL database name | `identity_db` |
| `POSTGRES_USER` | Yes | PostgreSQL username | `postgres` |
| `POSTGRES_PASSWORD` | Yes | PostgreSQL password | `secure_password` |
| `JWT_SECRET` | Yes | HMAC key for JWT signing (min 32 chars) | `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Yes | JWT token lifetime in milliseconds | `3600000` (1 hour) |
| `FRONTEND_URL` | Yes | CORS allowed origin | `http://localhost:3000` |
| `REDIS_HOST` | No | Redis host (default localhost) | `redis` |
| `REDIS_PORT` | No | Redis port (default 6379) | `6379` |
| `REDIS_PASSWORD` | No | Redis password | (empty for local) |
| `SECURITY_USER` | No | Spring Security default user | `admin` |
| `SECURITY_PASSWORD` | No | Spring Security default password | `admin` |
| `GOOGLE_CLIENT_ID` | No | Google OAuth2 Client ID | `xxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | No | Google OAuth2 Client Secret | `yyy` |
| `BASE_URL` | Yes | OAuth2 Authorization Server issuer | `http://localhost:8080` |

## Quick Start

### 1. Clone and Setup

```bash
git clone https://github.com/yourusername/identity-service.git
cd identity-service
```

### 2. Create Environment File

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

### 3. Start Infrastructure

```bash
docker compose up -d postgres redis
```

### 4. Run Application

```bash
./gradlew bootRun
```

### Or Use Docker Compose for Everything

```bash
docker compose up -d --build
```

Application will be available at `http://localhost:8080`

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description | Payload |
|--------|----------|-------------|---------|
| POST | `/auth/register` | Register new user | `{username, email, password}` |
| POST | `/auth/authenticate` | Login and get JWT | `{login, password}` |
| POST | `/auth/verify-email` | Verify email with token | `{token}` |
| POST | `/auth/resend-verification` | Resend verification email | `{email}` |

### Protected Endpoints (Require JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/POST/PUT/DELETE | `/api/**` | Protected API endpoints (application-specific) |

### OAuth2 Authorization Server

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/.well-known/openid-configuration` | OIDC discovery document |
| POST | `/oauth2/token` | Token endpoint (authorization_code, refresh_token) |
| GET | `/oauth2/authorize` | Authorization endpoint |
| GET | `/oauth2/jwks` | JSON Web Key Set endpoint |

### OAuth2 Login

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorization/google` | Redirect to Google login |

### Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/swagger-ui/index.html` | Interactive Swagger UI |
| GET | `/v3/api-docs` | OpenAPI 3.0 JSON specification |
| GET | `/actuator/health` | Health check endpoint |

## Usage Examples

### Register New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePassword123!"
  }'

# Response: 201 Created
# {
#   "message": "User registered successfully. Please check your email to verify your account."
# }
```

### Authenticate and Get JWT

```bash
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "login": "john_doe",
    "password": "SecurePassword123!"
  }'

# Response: 200 OK
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBl..."
# }
```

### Verify Email

```bash
curl -X POST http://localhost:8080/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Response: 200 OK
# {
#   "message": "Email verified successfully",
#   "verified": true
# }
```

### Resend Verification Email

```bash
curl -X POST http://localhost:8080/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com"
  }'

# Response: 200 OK
# {
#   "message": "Verification email sent successfully",
#   "verified": false
# }
```

### Request with JWT Token

```bash
curl http://localhost:8080/api/some-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Token is extracted, validated, and user is authenticated
```

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

From this token:
- `sub`: User ID (comes from SecurityContext)
- `roles`: User's assigned roles
- `iss`: Issuer name
- `iat`: Issued at (Unix timestamp)
- `exp`: Expiration (Unix timestamp)
- `jti`: Unique JWT ID

## Database Schema

### Users Table

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  permissions VARCHAR(255),
  account_state VARCHAR(50),
  security_status VARCHAR(50),
  email_verified BOOLEAN DEFAULT false,
  verified_at TIMESTAMP,
  last_verification_sent_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_users_account_state ON users(account_state);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email_verified ON users(email_verified);
```

### Roles Table

```sql
CREATE TABLE roles (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

-- Predefined roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_SUPER_ADMIN');
```

### Users-Roles Junction Table

```sql
CREATE TABLE users_roles (
  user_id UUID NOT NULL,
  role_id UUID NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

## Project Structure (Clean Architecture)

```
src/main/java/dev/mathalama/identityservice/

domain/                          [Business Logic & Rules]
├── entity/
│   ├── Users.java              [User aggregate root with UserDetails]
│   └── Role.java               [Role entity with GrantedAuthority]
├── enums/
│   ├── AccountState.java       [PENDING_VERIFICATION, ACTIVE, DISABLED, DELETED]
│   ├── SecurityStatus.java     [MFA_REQUIRED, MFA_VERIFIED, PENDING, VERIFIED]
│   └── KYCLifeCycle.java       [KYC pipeline states]
└── exception/
    ├── UnauthorizedException.java
    ├── UserAlreadyExistException.java
    └── UserNotFoundException.java

application/                     [Use Cases & DTOs]
├── dto/
│   ├── SignUpRegister.java     [Registration request]
│   ├── SignInRequest.java      [Login request]
│   ├── UserResponse.java       [User data response]
│   ├── VerifyEmailRequest.java [Email verification request]
│   ├── ResendVerificationRequest.java [Resend verification request]
│   ├── VerificationResponse.java [Verification status response]
│   ├── ErrorResponse.java      [Error response format]
│   └── UpdateRequest.java      [User update request]
└── service/
    ├── AuthService.java        [Interface: authentication use cases]
    ├── EmailService.java       [Interface: email sending]
    ├── JwtService.java         [Interface: JWT operations]
    ├── VerificationTokenService.java [Interface: token management]
    └── impl/
        └── AuthServiceImpl.java [Implementation of auth logic]

infrastructure/                  [External Integrations & Frameworks]
├── repository/
│   ├── UserRepository.java     [Spring Data JPA for users]
│   └── RoleRepository.java     [Spring Data JPA for roles]
├── service/
│   ├── EmailServiceImpl.java    [Async email sending via TaskExecutor]
│   ├── JwtServiceImpl.java      [JWT generation & validation]
│   └── VerificationTokenRedisService.java [Redis-based token storage]
└── config/
    ├── AsyncConfig.java        [Thread pool configuration]
    ├── RedisConfig.java        [Redis connection & serialization]
    ├── SecurityConfig.java     [Spring Security 3 filter chains]
    ├── JwtAuthenticationFilter.java [JWT → SecurityContext converter]
    ├── PasswordConfig.java     [BCryptPasswordEncoder bean]
    ├── WebConfig.java          [CORS configuration]
    └── FrontendProperties.java [@ConfigurationProperties for frontend URL]

presentation/                    [HTTP API Layer]
└── controller/
    └── AuthController.java     [REST endpoints: /auth/** routes]

IdentityServiceApplication.java [Spring Boot entry point]
```

## Security

### Password Security
- BCrypt hashing with salt
- Passwords never logged or returned
- Strong password requirements recommended

### Token Security
- JWT tokens signed with HMAC-SHA256
- Token expiration enforced (1 hour default)
- Access token includes user ID and roles
- No sensitive data in JWT payload

### Email Verification Security
- Verification tokens stored in Redis (not database)
- Tokens hashed with SHA-256 before Redis storage
- Raw token only exists in email link
- Token expires after 30 minutes
- One-time use: deleted after verification
- Resend rate-limited to 60-second cooldown

### Authentication Flow
```
Request → JwtAuthenticationFilter
         ├─ Extract Bearer token from Authorization header
         ├─ Validate signature with JWT_SECRET
         ├─ Check expiration
         ├─ Parse user ID and roles
         ├─ Load User from database
         └─ Set SecurityContext
            
Protected endpoint → @Secured/@PreAuthorize annotations
                    └─ Role checks applied by Spring Security
```

## Email Verification Flow

### Registration
```
1. User calls POST /auth/register
2. AuthService creates user with PENDING_VERIFICATION state
3. UUID token generated
4. Token hashed and stored in Redis with 30-min TTL
5. Raw token sent via email asynchronously
6. Response: instructions to check email
```

### Verification
```
1. User clicks email link with token
2. Frontend extracts token, calls POST /auth/verify-email
3. Token validated and located in Redis
4. User state changed to ACTIVE
5. Token deleted from Redis (one-time use)
6. Response: verification success
```

### Resend
```
1. User calls POST /auth/resend-verification with email
2. System checks if user is PENDING_VERIFICATION
3. System checks 60-second cooldown (stored in Redis)
4. New token generated (old automatically expires)
5. New email sent
6. Response: confirmation message
```

## Configuration

### Logging
Logs are output to console and can be configured via `application.yml`:

```yaml
logging:
  level:
    dev.mathalama.identityservice: DEBUG
    org.springframework.security: INFO
    org.springframework.web: INFO
```

### Database Connection Pooling
Default: HikariCP with 10 connections
Configure via environment or `application.yml`

### Redis Connection
Default: Lettuce driver with connection pooling
Configured automatically via Spring Boot Redis starter

## Development

### Build
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
```

### Format Code
```bash
./gradlew spotlessApply
```

### Generate Documentation
```bash
./gradlew bootJar
```

## Deployment

### Docker Build
```bash
docker build -t mathalama/identity-service:latest .
```

### Kubernetes
Update `docker-compose.yml` to use external database and Redis endpoints.

### Environment for Production

```bash
# Must set for production:
JWT_SECRET=<generate with: openssl rand -base64 32>
JWT_EXPIRATION=3600000
FRONTEND_URL=https://yourdomain.com
BASE_URL=https://api.yourdomain.com
POSTGRES_PASSWORD=<strong_password>
REDIS_PASSWORD=<strong_password>
```

## Monitoring & Health Checks

### Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

Response includes:
- Application status (UP/DOWN)
- Database availability
- Redis connectivity

### Metrics
Available at `/actuator/prometheus` for Prometheus scraping

### Logs
All operations logged with:
- User registration
- Authentication attempts
- Email verification events
- Token generation/validation
- Security events

## Troubleshooting

### Issue: "JWT claims string is empty"
Solution: Ensure Authorization header format is `Bearer <token>` with space

### Issue: "User not found"
Solution: Verify user exists and email is verified (state = ACTIVE)

### Issue: "Verification email was recently sent"
Solution: Wait 60 seconds before attempting resend

### Issue: "Invalid token"
Solution: Token may have expired (30-min window) or been used already

### Issue: Redis connection failed
Solution: Verify Redis is running and accessible at configured host:port

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Clean Architecture principles
4. Add tests for new features
5. Commit changes (`git commit -m 'Add amazing feature'`)
6. Push to branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check documentation in `/docs` directory
- Review [CLEAN_ARCHITECTURE_REFACTORING.md](CLEAN_ARCHITECTURE_REFACTORING.md) for architecture details
- See [API_QUICK_REFERENCE.md](API_QUICK_REFERENCE.md) for quick API examples

## Authors

[>] Identity Service Team

## Changelog

### v1.0.0 (February 27, 2026)
- Initial release
- JWT authentication with HMAC-SHA256
- OAuth2 Authorization Server and Client support
- Email verification with Redis token storage
- Clean Architecture implementation
- Role-based access control
- Async email sending
