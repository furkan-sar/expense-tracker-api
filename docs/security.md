# Security Design

## Authentication

Authentication method:

JWT

## Login Flow

1. User submits email and password.
2. Credentials are validated.
3. Access token is generated.
4. Refresh token is generated.
5. Tokens are returned to client.

## Access Token

Purpose:

* API authorization

Expiration:

15 minutes

## Refresh Token

Purpose:

* Obtain new access token

Expiration:

30 days

## Password Storage

Passwords must be stored using BCrypt hashing.

Plain text passwords must never be stored.

## Authorization

Budget access must be checked for every request.

Users may access only budgets where they are members.

## Roles

Budget Roles:

* OWNER
* ADMIN
* MEMBER
