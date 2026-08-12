# Feature Design: APX Issuance and Funding Lifecycle

**Status:** Proposed  
**Feature:** F-004 — APX issuance and funding lifecycle  
**Owner:** ApexLedger backend

## 1. Purpose

Define the controlled path by which APX first enters circulation. Without it, wallets can be created but no legitimate positive ledger balance exists for transfers.

## 2. Scope

### In scope

- Create one APX issuance record for each authorized supply increase.
- Post a balanced ledger journal that credits the recipient wallet and debits the system APX-issuance ledger account.
- Query issuance records and a wallet's funding history.
- Reverse an erroneous issuance through the F-003 reversal workflow.

### Out of scope

- Public minting, user self-funding, exchange purchases, fees, or external payment rails.
- Burning supply; add this only with an explicit supply-policy decision.
- Transfer requests; those are F-005.
- Authorization UI. Until roles exist, issuance endpoints are internal/admin only.

## 3. Lifecycle and accounting rules

```text
create request --> PENDING --> POSTED --> REVERSED
                       |\
                       `--> REJECTED
```

- `PENDING` records a validated request before accounting is posted.
- `POSTED` has exactly one posted journal entry and increases both recipient balance and APX circulating supply by the requested atomic amount.
- `REJECTED` has no journal entry and no balance effect.
- `REVERSED` references the reversing journal entry and removes the original supply effect through a new equal-and-opposite accounting event.
- An amount must be greater than zero and fit in signed `BIGINT` atomic units.
- An issuance has an idempotency key unique within its request scope. Repeating the same key returns the existing result; reusing it with a different payload returns `409 IDEMPOTENCY_KEY_CONFLICT`.

## 4. System accounts

F-003 provisions a special `SYSTEM` ledger account for `APX_ISSUANCE` during migration/bootstrap. It has `walletId = NULL` and `assetCode = APX`; the database's ledger-account ownership constraint explicitly permits this shape. The destination uses the wallet's APX ledger account. A posted issuance creates:

| Ledger account | Direction | Amount |
| --- | --- | --- |
| `APX_ISSUANCE` | Debit | requested atomic amount |
| Recipient wallet's ledger account | Credit | requested atomic amount |

Both lines resolve to APX ledger accounts, so the entry balances for APX while the credit-side wallet gains usable APX. Circulating supply is derived as the aggregate credit balance of APX wallet-owned ledger accounts, not stored as a mutable counter. Future fiat issuance uses a distinct system account for that asset; issuance never balances one asset against another.

## 5. API contract

`POST /api/v1/internal/issuances`

```json
{
  "destinationWalletId": "9dca4c11-526d-47ce-838b-f82ea6b38edf",
  "amountAtomic": 250000000,
  "idempotencyKey": "initial-demo-funding-001",
  "reason": "Initial simulated funding"
}
```

Successful response: `201 Created`

```json
{
  "id": "2d5f2d93-59fd-412d-b8a2-f5d0ed8c7270",
  "destinationWalletId": "9dca4c11-526d-47ce-838b-f82ea6b38edf",
  "amountAtomic": 250000000,
  "status": "POSTED",
  "journalEntryId": "d3c7393c-c2de-4be5-a27a-cdf460b19875",
  "createdAt": "2026-08-09T15:30:00Z",
  "postedAt": "2026-08-09T15:30:01Z"
}
```

The destination wallet must be `ACTIVE`; frozen wallets reject funding with `409 WALLET_NOT_ACTIVE`.

## 6. Persistence and failure handling

`apx_issuances` contains immutable request fields, status, journal and reversal references, timestamps, reason, and scoped idempotency key. Add foreign keys to wallet and journal tables; make the idempotency key unique.

| Situation | HTTP status | Code |
| --- | ---: | --- |
| Missing or invalid destination wallet | 404 | `WALLET_NOT_FOUND` |
| Frozen wallet | 409 | `WALLET_NOT_ACTIVE` |
| Zero, negative, or overflow amount | 400 | `INVALID_ATOMIC_AMOUNT` |
| Duplicate key, different request | 409 | `IDEMPOTENCY_KEY_CONFLICT` |
| Reversal requested twice | 409 | `ISSUANCE_NOT_REVERSIBLE` |

Creating the issuance record, posting the journal, and changing the issuance to `POSTED` must commit in one transaction.

## 7. Acceptance criteria

- A posted issuance creates exactly one balanced journal entry and credits the destination wallet by the requested atomic amount.
- Replaying the same request/key does not issue APX a second time.
- Frozen or nonexistent wallets never receive APX.
- Reversing an issuance creates an auditable compensating journal entry; no ledger history is edited.
- No floating-point monetary type appears in issuance APIs, domain objects, migrations, or tests.
