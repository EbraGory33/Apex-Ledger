# ADR 0004: Use a Domain-Driven Package Structure

## Status

Accepted

## Context

ApexLedger contains multiple business domains that will evolve over time. Organizing code only by technical layer, such as controllers, services, repositories, and entities, would make business boundaries harder to understand as the system grows.

The project should teach and demonstrate clean architecture, domain-driven design, SOLID principles, and modular software design.

## Decision

ApexLedger will organize backend code primarily by business domain. Each domain package will own its own domain, application, infrastructure, and interface layers.

The backend package root remains `com.apexledger`.

Initial banking-core modules should include:

```text
account
wallet
ledger
transfer
shared
```

Each major module should follow this structure:

```text
module
  domain
  application
  infrastructure
  interfaces
```

## Rationale

Business-first packaging keeps related code together and makes domain boundaries visible. It also prepares the codebase for future module extraction if a domain later becomes a microservice.

This structure is more maintainable than large horizontal packages such as `controller`, `service`, `repository`, and `entity`, especially as ApexLedger grows into blockchain, exchange, analytics, and fraud modules.

## Consequences

### Benefits

- Clear business boundaries.
- Better support for domain-driven design.
- Easier future service extraction.
- Less risk of god services.
- Easier navigation by feature or business capability.

### Tradeoffs

- Requires more upfront structure.
- May feel heavier than simple CRUD packaging.
- Developers must understand layer responsibilities.

## Layer Responsibilities

### Domain

Contains business objects, value objects, domain services, domain events, and repository interfaces.

### Application

Contains use-case services, commands, queries, DTOs, and orchestration logic.

### Infrastructure

Contains framework-specific adapters such as JPA persistence, external services, event publishers, and configuration.

### Interfaces

Contains HTTP controllers, request/response models, and API-facing adapters.
