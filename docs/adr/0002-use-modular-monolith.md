# ADR 0002: Use a Modular Monolith

## Status

Accepted

## Context

ApexLedger is intended to grow into a financial infrastructure simulation platform with domains such as accounts, wallets, ledger, transfers, blockchain settlement, exchange simulation, portfolio analytics, fraud detection, and authentication.

Although the long-term vision may eventually involve microservices, the early product needs strong business modeling, transactional consistency, fast refactoring, and simple local development.

Starting with microservices too early would add distributed systems complexity before the core domain model is stable.

## Decision

ApexLedger will begin as a modular monolith.

Each business capability will own its own package boundary. Modules should be designed so they can potentially be extracted into services later, but they will initially run in one deployable backend application.

## Rationale

A modular monolith provides the best balance for the early stage of ApexLedger. It allows the project to model real fintech concepts without prematurely introducing network boundaries, distributed transactions, service discovery, independent deployment pipelines, and cross-service observability concerns.

The goal is to make domain boundaries clear in code while keeping operational complexity low.

## Consequences

### Benefits

- Simpler local development.
- Easier refactoring while the domain is still evolving.
- Easier transaction management across early banking workflows.
- Lower infrastructure complexity.
- Clear path toward future service extraction.

### Tradeoffs

- Module boundaries require discipline.
- Developers must avoid direct cross-module coupling.
- A poorly maintained modular monolith can still become a big ball of mud.
- Future microservice extraction must be deliberate, not accidental.

## Module Boundary Rules

- Business logic belongs in domain and application layers, not controllers.
- Domain objects should not depend on Spring, JPA, HTTP, Kafka, Redis, or cloud services.
- Infrastructure adapters may depend on frameworks and databases.
- Controllers should call application services rather than repositories directly.
- Cross-module interaction should happen through application services, domain events, or carefully designed interfaces.
