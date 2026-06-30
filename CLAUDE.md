# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OutVoice is an invoicing backend built with Spring Boot 3.5.14 / Java 21. It handles user auth, organization management, and PDF invoice generation. The app is in early development — only the initial scaffold exists so far.

## Commands

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run (requires PostgreSQL running)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Stack & Key Dependencies

| Concern | Library |
|---|---|
| Web / REST | Spring Web (Servlet) |
| Auth | Spring Security + JJWT 0.12.6 |
| Persistence | Spring Data JPA + PostgreSQL |
| Migrations | Flyway (PostgreSQL dialect) |
| PDF generation | OpenPDF 2.0.3 |
| Email | Spring Mail |
| Boilerplate reduction | Lombok |
| Rate limiting | Bucket4j 8.18.0 |

## Architecture

Base package: `com.omkarsathe.outvoice`

Intended layering (feature-based slices inside the base package):

```
outvoice/
  auth/           # JWT filter, SecurityConfig, signup/login endpoints
  user/           # User entity, repository, service
  organization/   # Org entity + tax details (PAN/GST/TAN)
  invoice/        # Invoice entity, PDF generation via OpenPDF
  common/         # Shared utilities, exceptions, response wrappers
```

Flyway migration scripts go in `src/main/resources/db/migration/` with the standard `V{n}__{description}.sql` naming.

## Configuration

`src/main/resources/application.yaml` specifies defaults. Environment-specific values should be provided via environment variables or a local `application-local.yaml` (gitignored).

### Required Env Vars & Defaults
- `DB_URL`: JDBC database URL (default: `jdbc:postgresql://localhost:5432/outvoice`)
- `DB_USERNAME`: Database username (default: `postgres`)
- `DB_PASSWORD`: Database password (default: `local-db@123`)
- `JWT_SECRET`: Base64-encoded 256-bit signing key (required)
- `JWT_EXPIRY_MS`: JWT expiry in milliseconds (default: `86400000` / 24 hours)
- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed CORS origins (default: `http://localhost:4200`)
- `RATE_LIMIT_AUTH_CAPACITY`: Max login/signup requests per IP per window (default: `10`)
- `RATE_LIMIT_AUTH_REFILL_SECONDS`: Refill rate window in seconds (default: `60`)

### Running Tests
To run tests locally when environment variables are not globally defined, specify them inline (e.g. setting `JWT_SECRET` and setting `JAVA_HOME` if Java 21 is not your default):
```powershell
$env:JAVA_HOME="C:\Users\Hp\.jdks\jbr-21.0.10"; $env:JWT_SECRET="dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWRldmVsb3BtZW50LXBVcnBvc2VzLW9ubHk="; .\mvnw.cmd test
```


## Auth Design

- Stateless JWT-based auth; no sessions.
- Signup accepts email **or** mobile number (not both required), plus organization details.
- Tokens signed with HS256 via JJWT; filter chain should extend `OncePerRequestFilter`.
