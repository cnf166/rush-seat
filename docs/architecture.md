# RushSeat — Architecture Overview

## 1. Purpose

RushSeat is a backend system for high-concurrency flash sales and ticket reservations.

The system must prevent overselling, maintain consistent inventory, handle duplicate requests, and release expired reservations correctly.

The initial implementation uses a modular monolith backed by PostgreSQL.

## 2. Architectural Style

RushSeat follows a layered architecture with explicit boundaries between HTTP handling, business logic, persistence, and external integrations.

```text
Client
  |
  v
API Layer
  |
  v
Business Layer
  |
  v
Data / Infrastructure Adapters
  |
  v
PostgreSQL
```

The business layer owns use cases and business rules. Data and infrastructure adapters implement the interfaces required by the business layer.

## 3. Layer Responsibilities

### API

Responsibilities:
- Handle HTTP requests and responses.
- Validate request DTOs.
- Map requests to business inputs.
- Translate application errors into HTTP responses.

The API layer must not contain inventory management logic or database queries.

### Business

Responsibilities:
- Implement application use cases.
- Enforce reservation lifecycle rules.
- Define domain models and business abstractions.
- Coordinate operations that must succeed or fail together.

The business layer must not depend directly on Spring Data JPA, Hibernate, Redis clients, or Kafka clients.

### Data

Responsibilities:
- Define persistence entities.
- Implement database repository adapters.
- Map between persistence entities and business models.
- Support transactional persistence operations.

Database constraints and transactions must protect critical inventory invariants.

### Infrastructure

Responsibilities:
- Integrate external systems.
- Provide Redis, Kafka, and payment adapters when required.
- Handle infrastructure-specific configuration and failure scenarios.

Infrastructure integrations are introduced only when a concrete requirement justifies them.

### Security

Responsibilities:
- Authenticate users.
- Enforce role-based authorization.
- Protect resources according to ownership and permissions.

Resource identifiers must never be treated as authorization credentials.

### Config

Responsibilities:
- Define application configuration.
- Configure beans and infrastructure integration points.
- Keep environment-specific settings outside business logic.

## 4. Initial Technology Choices

- Java 21
- Spring Boot 4.1.1
- Gradle Kotlin DSL
- Spring MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway for database migrations
- JUnit 5 and Mockito

Redis, Kafka, Docker-based deployment, and cloud infrastructure will be introduced in later phases.

## 5. Reservation and Inventory

The initial MVP uses general-admission tickets without assigned seats.

Each ticket type maintains:
- `total_quantity`
- `held_quantity`
- `sold_quantity`

Available inventory is derived as:

`available_quantity = total_quantity - held_quantity - sold_quantity`

The following invariant must always hold:

`0 <= held_quantity + sold_quantity <= total_quantity`

Reservation state transitions and corresponding inventory updates must be atomic.

PostgreSQL is the authoritative source for reservation state and persistent inventory.

## 6. Reliability and Concurrency

The system must support:
- Transaction-safe inventory updates.
- Protection against overselling.
- Idempotent reservation creation.
- Safe confirmation, cancellation, and expiration.
- Integration tests for concurrent operations.
- Reproducible performance benchmarks.

Correctness is established before introducing caching or asynchronous processing.

## 7. Evolution Strategy

1. Implement the core REST API and reservation lifecycle.
2. Add PostgreSQL persistence and concurrency control.
3. Evaluate Redis for atomic reservation operations and performance.
4. Introduce payment simulation, idempotency, and Kafka where justified.
5. Add integration tests, load tests, CI/CD, and deployment.

Each new infrastructure component must have a defined responsibility, failure-handling strategy, and measurable benefit.
