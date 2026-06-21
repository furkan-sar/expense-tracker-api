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
