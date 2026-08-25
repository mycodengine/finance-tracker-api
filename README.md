# Finance Tracker API

A production-quality REST API for personal finance tracking — built as a GitHub showcase project demonstrating clean Java architecture, proper testing, and maintainable code.

## Features

- **JWT Authentication** — stateless auth with access + refresh tokens
- **Accounts** — manage multiple financial accounts (Checking, Savings, Credit, Cash)
- **Transactions** — record income/expense transactions with automatic balance updates
- **Categories** — system defaults + user-defined categories with colors and icons
- **Reports** — monthly income vs expense summaries, category spending breakdowns, and balance history
- **Pagination & Filtering** — transactions support date range, type, category, and account filters

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Build | Gradle (Kotlin DSL) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + JWT (JJWT 0.12) |
| DTO Mapping | MapStruct |
| DB Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Unit Tests | JUnit 5 + Mockito |
| Integration Tests | Spring Boot Test + MockMvc + Testcontainers |
| Code Quality | Checkstyle |

## Project Structure

```
src/main/java/com/financetracker/
├── config/          # Security, OpenAPI configuration
├── controller/      # REST controllers (thin — delegate to services)
├── domain/
│   ├── entity/      # JPA entities
│   └── enums/       # TransactionType, AccountType
├── dto/
│   ├── request/     # Validated request records
│   └── response/    # Response records
├── exception/       # Custom exceptions + global handler
├── mapper/          # MapStruct mappers
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, JwtService, UserDetailsService
└── service/         # Business logic
```

## Getting Started

### Prerequisites

- Java 21
- Docker & Docker Compose

### 1. Clone and configure

```bash
git clone https://github.com/your-username/finance-tracker-api.git
cd finance-tracker-api
cp .env.example .env
# Edit .env with your preferred credentials
```

### 2. Start the database

```bash
docker-compose up -d
```

### 3. Run the application

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The API will be available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running Tests

```bash
# All tests (unit + integration via Testcontainers)
./gradlew test

# Tests + Checkstyle
./gradlew check
```

> Integration tests spin up a real MySQL container via Testcontainers — no H2 shortcuts.

## API Overview

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/v1/auth/register`, `/login`, `/refresh` |
| Accounts | `GET/POST /api/v1/accounts`, `GET/PUT/DELETE /api/v1/accounts/{id}` |
| Categories | `GET/POST /api/v1/categories`, `PUT/DELETE /api/v1/categories/{id}` |
| Transactions | `GET/POST /api/v1/transactions`, `GET/PUT/DELETE /api/v1/transactions/{id}` |
| Reports | `GET /api/v1/reports/summary`, `/by-category`, `/balance-history` |

Full documentation available via Swagger UI.

## Environment Variables

| Variable | Description | Default (dev) |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/finance_tracker` |
| `DB_USERNAME` | DB username | `finance_user` |
| `DB_PASSWORD` | DB password | — |
| `JWT_SECRET` | Base64-encoded 256-bit key | Dev placeholder (change in prod!) |

## License

MIT
