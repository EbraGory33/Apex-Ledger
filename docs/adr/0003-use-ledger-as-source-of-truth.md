# ADR 0003: Use the Ledger as the Source of Truth

## Status

Accepted

## Context

ApexLedger simulates a financial platform using fictional digital currency. Even though APX is not real money, the system should model financial behavior using realistic engineering practices.

A simple wallet balance column would be easy to implement, but it would not teach or demonstrate the accounting discipline used by serious financial systems.

ApexLedger needs to support transfers, reversals, audit history, blockchain-inspired settlement, exchange trades, fraud analysis, and portfolio calculations. These features require reliable historical accounting records.

## Decision

The ledger will be the source of truth for financial balances.

Wallet balances may be exposed through read models or projections, but authoritative financial state will come from immutable ledger records.

Balance-changing operations must create balanced accounting records rather than simply mutating a wallet balance field.

## Rationale

A ledger-first design is closer to how banks, payment companies, trading platforms, and crypto custodians model value movement. It supports auditability, reconciliation, reversals, historical analysis, and future settlement workflows.

This decision makes the project more complex than a CRUD wallet application, but it also makes ApexLedger more valuable as a realistic fintech platform.

## Consequences

### Benefits

- Strong audit trail.
- Reconstructable balances.
- Better support for reversals and corrections.
- Better foundation for portfolio analytics.
- Better foundation for fraud detection.
- Better foundation for blockchain-style settlement history.

### Tradeoffs

- More complex domain model.
- Balance queries may require projections for performance.
- Developers must understand debit and credit semantics.
- Tests must verify accounting invariants.

## Accounting Rules

- Ledger entries are append-only.
- Posted entries must not be edited.
- Corrections require reversal entries.
- Every journal entry must balance.
- Total debits must equal total credits for each single-currency journal entry.
- Money must not be represented with floating point types.
- Wallet-facing balances should be derived or projected from ledger activity.
