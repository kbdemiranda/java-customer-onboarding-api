# Project: Customer Onboarding API

## Purpose

This project implements a banking customer onboarding API.

It handles:

* customer creation
* address validation via zip code
* document upload
* audit logging

---

## Architecture

* Layered architecture:
  Controller → Service → Repository / Client

Rules:

* Controllers must be thin
* Services contain business logic
* Repositories handle persistence only
* Clients handle external integrations (WireMock / ViaCEP)

---

## Coding Standards

* Java 21
* Spring Boot 4
* Maven
* Constructor injection only
* Use Lombok when appropriate
* Use records for DTOs when possible
* Use `application.yml` (no `.properties` file)

Naming:

* Use English for all backend code
* Use `zipCode` instead of `cep` in Java
* Keep `cep` only in external API responses if required
* Frontend (if any) remains in Portuguese

---

## Database Rules

* PostgreSQL
* Flyway migrations only (no schema auto-creation)
* Do NOT modify previous migrations
* Always create new migration files
* Use `BIGSERIAL` for internal IDs
* Use `UUID externalId` for public identifiers

---

## Domain Invariants

* A CustomerOnboarding must always have a valid CPF
* CPF must be unique per onboarding record
* A CustomerOnboarding must have at least one primary contact method (email or phone) at creation time
* At most one primary address, email, and phone per onboarding
* Addresses must be enriched via zip code providers before persistence

---

## Validation Rules

* Use Bean Validation
* Use CPF validation annotation (Hibernate Validator)
* Normalize inputs (`cpf`, `zipCode`)
* Accept formatted or unformatted input when possible

---

## External Services

Zip code lookup:

* Interface: `ZipCodeClient`
* Implementations:

  * `WireMockZipCodeClient`
  * `ViaCepZipCodeClient`

Fallback strategy:

1. Try WireMock
2. If not found, fallback to ViaCEP

---

## Error Handling

* Use `@RestControllerAdvice`

Standard error response:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "..."
}
```

---

## API Design

* RESTful
* Use `/api/v1` prefix
* Use `UUID externalId` in endpoints
* Do not expose internal database IDs

---

## Testing

* Unit tests for services
* Controller tests for endpoints
* Integration tests using H2 (optional)

---

## Git Rules

VERY IMPORTANT:

After each feature:

* `git add .`
* `git commit -m "feat: <feature description>"`

Rules:

* One feature per commit
* Do NOT group unrelated changes
* Use clear and descriptive commit messages

Examples:

* `feat: create onboarding entity`
* `feat: implement zip code lookup`
* `feat: add document upload validation`

---

## Development Strategy

* Build in small steps
* Always keep the project compiling
* Do NOT implement everything at once
* Prioritize MVP delivery

---

## What NOT to do

* Do NOT implement authentication
* Do NOT implement OCR
* Do NOT over-engineer
* Do NOT add unnecessary abstractions
* Do NOT break existing code

---

## Goal

Deliver a clean, maintainable, production-like backend API suitable for a banking onboarding scenario.
