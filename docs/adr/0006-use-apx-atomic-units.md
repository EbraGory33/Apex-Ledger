# ADR 0006: Represent Assets Using Atomic Units

## Status

Accepted

## Context

ApexLedger begins with a simulated digital currency called APX and is expected to support real currencies later. Financial systems must avoid floating-point arithmetic for money because binary floating point can introduce precision errors. Different assets also have different display precision: APX uses eight decimal places, USD commonly uses two, and JPY uses zero.

## Decision

All asset amounts will be represented using signed 64-bit integer atomic units. Precision belongs to the asset definition rather than being a global constant.

The initial asset definition is:

```text
APX decimal_places = 8
1 APX = 100,000,000 atomic units
```

Supported assets are defined in an `assets` table. A ledger account references one asset, and a ledger line inherits its asset from that account. Journal balance is evaluated independently for each represented asset.

## Rationale

Atomic units provide deterministic arithmetic and are familiar in payment and cryptocurrency systems. This approach maps well to immutable ledger entries and avoids rounding errors in transfers, balances, exchange orders, and portfolio calculations. Per-asset precision lets USD use two decimal places while APX uses eight without changing ledger amount types.

An asset catalog is stricter than a free-form `VARCHAR` foreign key target and more extensible than a PostgreSQL enum or an ever-growing `CHECK (code IN (...))`. It also provides the precision metadata needed at system boundaries.

## Consequences

### Benefits

- Avoids floating-point precision bugs.
- Supports asset-specific precision.
- Works well with ledger entries.
- Makes balances deterministic and auditable.
- Rejects unregistered asset codes at the database boundary.

### Tradeoffs

- User-facing APIs must format atomic units using the referenced asset's precision.
- Developers must understand the difference between display amounts and stored atomic amounts.
- Every monetary amount must carry or resolve an asset context; a bare integer is not sufficient at a system boundary.
- Supporting an asset requires an intentional catalog entry and associated business policy.

## Rules

- Store all monetary amounts as integer atomic units.
- Validate that amounts are positive for financial movement.
- Convert to display decimals only at system boundaries.
- Define supported assets with a constrained uppercase code, name, and `decimal_places` from 0 through 18.
- Make ledger accounts reference the asset catalog; never accept an arbitrary string as accounting identity.
- Balance journals independently per asset. Equal numeric amounts in different assets do not balance one another.
- Cross-asset conversion requires explicit foreign-exchange accounting and is outside the initial scope.
