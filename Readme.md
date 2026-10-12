# RushSeat

**High-Concurrency Flash-Sale & Seat Reservation System**

RushSeat is a Java backend project focused on concurrency-safe ticket reservations, inventory consistency, idempotent operations, and reliable reservation expiration.

The project explores how to design and test a reservation system that handles concurrent requests without overselling or processing the same business operation more than once.

## Goals

- Prevent ticket overselling under concurrent requests.
- Implement a reliable reservation lifecycle with expiration.
- Handle duplicate requests through idempotency.
- Maintain inventory consistency using PostgreSQL transactions.
- Measure performance using reproducible load tests.
- Document technical decisions, trade-offs, and failure scenarios.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- JUnit 5, Mockito, Testcontainers
- k6
- Docker and Docker Compose
- GitHub Actions

Redis, Kafka, and AWS deployment will be introduced in later phases when specific requirements justify their use.

## Running Locally

Requirements:

- JDK 21
- Gradle Wrapper included in the repository

Run tests:

```bash
./gradlew clean test
```

Run the application:

```bash
./gradlew bootRun
```

## Project Status

Initial project setup and design phase.
