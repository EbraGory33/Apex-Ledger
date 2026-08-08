# ADR 0001: Keep Java 25 and the Existing Development Environment

## Status

Accepted

## Context

ApexLedger is a long-term financial infrastructure simulation platform. The current backend project has already been generated and configured with Java 25 and the existing Spring Boot environment.

The project also currently has the JPA starter dependency commented out. This is intentional for now because enabling it immediately causes the development environment to crash before persistence has been deliberately configured.

ApexLedger is still in its architecture and domain-design phase. At this stage, avoiding unnecessary environment churn is more valuable than changing versions or forcing persistence dependencies before the application is ready for them.

## Decision

We will keep Java 25 and the existing project environment.

We will also keep `spring-boot-starter-data-jpa` commented out until the persistence layer is intentionally introduced with database configuration, migrations, and validation rules.

## Rationale

Keeping the existing environment allows development to proceed without spending early project time on runtime churn. Java 25 gives the project a modern runtime baseline, and preserving the existing setup keeps focus on the most important early work: domain modeling, architecture boundaries, and the accounting model.

Deferring JPA activation is also intentional. Persistence should be introduced as an architectural decision, not as an accidental dependency toggle. When JPA is enabled, it should be accompanied by PostgreSQL configuration, migration tooling, and a clear decision about schema ownership.

## Consequences

### Benefits

- The project avoids unnecessary environment changes.
- The team can focus on architecture and domain modeling first.
- Java 25 remains the standard runtime for ApexLedger.
- The application avoids crashing due to incomplete persistence configuration.
- JPA can be introduced later with proper database discipline.

### Tradeoffs

- Some enterprise examples and tutorials may assume Java 21 or Spring Boot 3.
- Some libraries may need compatibility checks against the selected environment.
- Persistence work is delayed until the database foundation is ready.
- Future contributors must understand that commented JPA is intentional, not forgotten.

## Follow-Up Actions

- Document database setup before enabling JPA.
- Choose and configure a migration tool before creating persistence entities.
- Keep Hibernate schema generation disabled or limited once migrations exist.
- Revisit the JPA dependency when implementing the first persisted aggregate.
