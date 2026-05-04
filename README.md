# Customer Onboarding API

Backend API for a banking customer onboarding technical challenge.

<!-- TODO: Add API demo GIF here -->

## Project Overview
This project provides a REST API to onboard bank customers, including customer registration, zip code enrichment, document upload, and audit history tracking.

## Banking Context
Banks need a controlled onboarding process to collect customer identity data, validate mandatory fields, receive supporting documents, and keep an auditable history of actions.

## Main Features
- Create customer onboarding with CPF validation and normalization.
- Enrich addresses from zip code providers (WireMock first, ViaCEP fallback).
- Upload onboarding documents with type and content validation.
- List onboardings with pagination and filters.
- Retrieve onboarding details, uploaded documents, and audit logs.
- Standardized error response format.

## Tech Stack
- Java 21
- Spring Boot 4
- Spring Web / Validation / Data JPA
- PostgreSQL
- Flyway
- Maven
- springdoc-openapi (Swagger UI)

## Architecture
Layered architecture:
- Controller -> Service -> Repository / Client

<!-- TODO: Add Mermaid architecture diagram here -->

<!-- TODO: Add Mermaid onboarding flow diagram here -->

## How to Run Locally
### Prerequisites
- Java 21
- Docker and Docker Compose (recommended)
- Maven Wrapper (`./mvnw`)

### 1. Start dependencies
```bash
docker compose up -d
```

### 2. Run the API
```bash
./mvnw spring-boot:run
```

The API runs by default at `http://localhost:8080`.

## Docker Compose
The included `docker-compose.yml` starts:
- PostgreSQL database
- WireMock server for zip code mock responses

Start:
```bash
docker compose up -d
```

Stop:
```bash
docker compose down
```

## Environment Variables
Key variables (with defaults from `application.yml`):
- `SERVER_PORT` (default: `8080`)
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/customers_onboarding`)
- `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default: `postgres`)
- `POSTGRES_USER` (used by Docker Compose)
- `POSTGRES_PASSWORD` (used by Docker Compose)

## API Endpoints
Base path: `/api/v1/onboardings`
- `POST /api/v1/onboardings`
- `GET /api/v1/onboardings`
- `GET /api/v1/onboardings/{externalId}`
- `POST /api/v1/onboardings/{externalId}/documents`
- `GET /api/v1/onboardings/{externalId}/documents`
- `GET /api/v1/onboardings/{externalId}/audit-logs`

## Example cURL Requests
### Create onboarding
```bash
curl -X POST http://localhost:8080/api/v1/onboardings \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "cpf": "123.456.789-09",
    "emails": [{"email": "john@example.com", "primaryEmail": true}],
    "phones": [{"phoneNumber": "11999999999", "primaryPhone": false}],
    "addresses": [{"zipCode": "01001-000", "number": "100", "complement": "Apt 10", "primaryAddress": true}]
  }'
```

### List onboardings
```bash
curl "http://localhost:8080/api/v1/onboardings?page=0&size=10&status=DOCUMENTS_PENDING"
```

### Get onboarding by externalId
```bash
curl http://localhost:8080/api/v1/onboardings/{externalId}
```

### Upload document
```bash
curl -X POST "http://localhost:8080/api/v1/onboardings/{externalId}/documents" \
  -F "file=@/path/to/cpf.pdf" \
  -F "documentType=CPF"
```

### List documents
```bash
curl http://localhost:8080/api/v1/onboardings/{externalId}/documents
```

### List audit logs
```bash
curl http://localhost:8080/api/v1/onboardings/{externalId}/audit-logs
```

## Swagger / OpenAPI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing
Run all tests:
```bash
./mvnw clean test
```

## Technical Decisions
- Public API exposes only `externalId` (UUID), never internal DB IDs.
- Input normalization is applied for identifiers like CPF and zip code.
- Address enrichment uses provider fallback strategy (WireMock -> ViaCEP).
- Global exception handling returns a consistent error contract.
- Flyway is used for schema migration control.

## Future Improvements
- Improve observability with structured logs and trace correlation.
- Add integration tests with Testcontainers for PostgreSQL.
- Add rate limiting and resilience patterns for external provider calls.
- Add asynchronous document processing pipeline if needed.
