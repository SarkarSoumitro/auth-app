# Auth App — Spring Boot JWT Authentication & RBAC Service

A production-style authentication and authorization backend built with **Spring Boot 4**, **Spring Security**, and **JWT**, featuring role-based access control (RBAC), refresh token rotation, and an admin API for user/role management.


---

## Features

- **JWT-based authentication** — stateless access tokens signed and verified via `jjwt`
- **Refresh token flow** — long-lived refresh tokens for silent re-authentication without repeated logins
- **Token revocation** — admins can invalidate a user's refresh tokens on demand (e.g. for compromised accounts or forced logout)
- **Role-Based Access Control (RBAC)** — fine-grained authorization via Spring Security's `hasAuthority(...)`, with roles stored as a proper many-to-many relationship rather than a hardcoded enum
- **Admin management API** — list/search users, replace or append roles, remove individual roles, and manage the role catalog itself
- **UUID primary keys** — entities use database-generated UUIDs (`@GeneratedValue(strategy = GenerationType.UUID)`) rather than client-supplied or sequential IDs
- **Validated request DTOs** — input validation via `spring-boot-starter-validation`
- **Clean layered architecture** — controller → service → repository, with DTO/entity separation enforced via ModelMapper

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security |
| Data access | Spring Data JPA |
| Database | MySQL |
| Auth tokens | JJWT (`io.jsonwebtoken`) 0.13.0 |
| Object mapping | ModelMapper |
| Validation | Spring Validation |
| Build tool | Maven |
| Boilerplate reduction | Lombok |

---

## Architecture

```
Controller Layer   →  REST endpoints, request/response DTOs, @PreAuthorize checks
Service Layer      →  Business logic, role assignment rules, token lifecycle
Repository Layer   →  Spring Data JPA repositories
Entity Layer        →  JPA entities with UUID-generated primary keys
Security Layer     →  JWT filter chain, authentication provider, RBAC config
```

**Design decisions worth noting:**

- **IDs are never client-supplied.** DTOs used for *creation* requests omit the `id` field entirely, letting Hibernate's `persist()` (not `merge()`) assign a fresh UUID. This avoids `ObjectOptimisticLockingFailureException` caused by treating new entities as detached/existing ones.
- **Roles are additive or replaceable by design**, not just deletable — the API distinguishes between *replacing* a user's entire role set (`PATCH`) and *appending* to it (`POST`), matching proper REST semantics rather than collapsing both into one ambiguous endpoint.

---

## API Overview

All admin endpoints require an authenticated request with `ADMIN` authority (`@PreAuthorize("hasAuthority('ADMIN')")`).

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Authenticate and receive access + refresh tokens |
| `POST` | `/api/v1/auth/refresh` | Exchange a valid refresh token for a new access token |

### Admin — User Management
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/admin/users` | List all users |
| `GET` | `/api/v1/admin/users/{email}` | Get a single user by email |
| `PATCH` | `/api/v1/admin/users/{email}/roles` | Replace a user's entire role set |
| `POST` | `/api/v1/admin/users/{email}/roles` | Add one or more roles to a user |
| `DELETE` | `/api/v1/admin/users/{email}/roles/{roleName}` | Remove a single role from a user |
| `POST` | `/api/v1/admin/users/{email}/revoke-tokens` | Revoke all refresh tokens for a user (forces re-authentication) |

### Admin — Role Management
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/admin/roles` | List all available roles |
| `POST` | `/api/v1/admin/roles` | Create a new role |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8+

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/SarkarSoumitro/auth-app.git
   cd auth-app
   ```

2. **Create a MySQL database**
   ```sql
   CREATE DATABASE auth_app;
   ```

3. **Configure application properties**

   Set the following in `src/main/resources/application.properties` (or via environment variables):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/auth_app
   spring.datasource.username=your_username
   spring.datasource.password=your_password

   jwt.access-token.ttl=900000
   jwt.refresh-token.ttl=604800000
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   The app starts on `http://localhost:8080` by default.

5. **Run tests**
   ```bash
   ./mvnw test
   ```

---

## Roadmap

- [ ] Swagger / OpenAPI documentation
- [ ] Integration tests for the admin role-management flow
- [ ] Access token revocation (blacklist) for immediate session termination, not just refresh token revocation
- [ ] Rate limiting on `/auth/login` and `/auth/refresh`
- [ ] Docker Compose setup for local MySQL + app

---

## Author

**Soumitro Sarkar**
B.Sc. in Computer Science & Engineering, United International University
[GitHub](https://github.com/SarkarSoumitro) · [LinkedIn](https://linkedin.com)
