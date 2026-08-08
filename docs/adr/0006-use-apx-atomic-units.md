# ADR 0006: Represent APX Using Atomic Units

## Status

Accepted

## Context

ApexLedger uses simulated digital currency called APX. Financial systems must avoid floating point arithmetic for money because binary floating point can introduce precision errors.

APX should behave like a realistic digital asset while remaining fictional.

## Decision

APX amounts will be represented using atomic units.

The initial precision rule is:

```text
1 APX = 100,000,000 atomic units
```

Domain code should not use floating point types such as `double` or `float` for monetary values.

## Rationale

Atomic units provide deterministic arithmetic and are familiar in cryptocurrency-style systems. This approach also maps well to immutable ledger entries and avoids rounding errors in transfers, balances, exchange orders, and portfolio calculations.

## Consequences

### Benefits

- Avoids floating point precision bugs.
- Supports crypto-like precision.
- Works well with ledger entries.
- Makes balances deterministic and auditable.

### Tradeoffs

- User-facing APIs must format atomic units into decimal APX values.
- Developers must understand the difference between display amount and stored amount.
- Future assets may need their own precision metadata.

## Rules

- Store APX amounts as integer atomic units.
- Validate that amounts are positive for financial movement.
- Convert to display decimals only at system boundaries.
- Keep currency precision explicit when additional assets are introduced.
