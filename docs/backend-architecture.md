# Backend Architecture

## Package Structure

* controller
* service
* repository
* entity
* dto
* mapper
* config
* security
* exception

## Layer Responsibilities

### Controller

Responsibilities:

* Handle HTTP requests
* Validate request payloads
* Return API responses

Must not contain business logic.

### Service

Responsibilities:

* Business rules
* Validation beyond HTTP validation
* Authorization checks
* Transaction management

### Repository

Responsibilities:

* Database access
* Queries
* Persistence operations

### Entity

Responsibilities:

* Database mapping
* JPA annotations

Must not be exposed through REST APIs.

### DTO

Responsibilities:

* Request payloads
* Response payloads

### Mapper

Responsibilities:

* Entity to DTO conversion
* DTO to Entity conversion

MapStruct should be used.

## General Rules

* Use constructor injection.
* Avoid field injection.
* Use global exception handling.
* Keep controllers thin.
* Keep services cohesive.
