# Backend Agent Rules

## Contract Rules

Backend must follow:

contracts/openapi/expense-tracker-api.yaml

Rules:

* Do not create undocumented endpoints.
* Do not modify request/response models without updating the contract.
* Every API change must update the OpenAPI contract.
* Every API change must update documentation if required.
* DTOs must be separated from entities.
* Controllers must never return entities directly.

## Architecture Rules

Package structure:

* controller
* service
* repository
* entity
* dto
* mapper
* config
* security
* exception

Rules:

* Controller handles HTTP requests and responses only.
* Service contains business logic.
* Repository contains database access only.
* Entity is persistence model.
* DTO is API model.
* Mapper converts entity and DTO objects.

## Security Rules

* Use JWT authentication.
* Use Refresh Token mechanism.
* Passwords must be hashed using BCrypt.
* Never expose password fields in API responses.

## Environment Rules

Avoid running:

* mvn test
* mvn clean install
* mvn spring-boot:run
* docker compose up

Unless explicitly requested.

Modify source code and explain required local commands instead.

## Logging Rules

- Every request must have a requestId.
- RequestId must be stored in MDC.
- All exceptions must be logged.
- Business exceptions use WARN level.
- Unexpected exceptions use ERROR level.
- SQL logging enabled only in development profile.
- Never log passwords.
- Never log JWT tokens.
- Never log refresh tokens.
- Log service start and completion for important business operations.

## PostgreSQL Query Rules

- Avoid native queries when dynamic filtering is required.
- Prefer JPA Specifications, Criteria API or Querydsl for optional filters.
- Do not use patterns like:
  (:param IS NULL OR column = :param)
  inside PostgreSQL native queries.

- Optional filters must be added dynamically.
- PostgreSQL nullable parameter type inference issues must be avoided.

Examples of optional filters:

- budgetGroupId
- categoryId
- memberId
- transactionType
- startDate
- endDate

When using native queries:

- Explicitly cast nullable parameters.
- UUID parameters must be cast to UUID.
- Date parameters must be cast to DATE.
- Enum/String parameters must be cast to VARCHAR.

All report queries must support null filters without throwing SQL errors.