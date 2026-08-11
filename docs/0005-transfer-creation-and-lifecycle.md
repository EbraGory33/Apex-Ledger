# Feature Design: APX Transfer Creation and Lifecycle

**Status:** Proposed  
**Feature:** F-005 — APX transfer creation and lifecycle  
**Owner:** ApexLedger backend

## 1. Purpose

Move APX between two wallets through one auditable, ledger-backed transfer. Transfer is the first user-facing financial operation and must never mutate a wallet balance directly.

## 2. Scope

### In scope

- Create an idempotent wallet-to-wallet APX transfer request.
- Validate both wallets, their lifecycle states, distinct ownership, and source available balance.
- Atomically post a balanced journal entry and mark the transfer `POSTED`.
- Retrieve a transfer and list a wallet's transfers.
- Reverse a posted transfer through a compensating journal entry.

### Out of scope

- Fees, scheduled transfers, holds, limits, external recipients, notifications, and cross-asset transfers.
- Concurrent reservation/hold mechanics beyond transaction-safe insufficient-funds protection.
- Authentication and authorization; the current caller is not yet tied to the source account.
- Blockchain settlement. A later feature consumes `POSTED` transfer events.

## 3. Lifecycle

```text
create request --> PENDING --> POSTED --> REVERSED
                       |\
                       `--> REJECTED
```

- `PENDING` exists only while validation and ledger posting are in progress.
- `POSTED` has exactly one posted journal entry and is final except for reversal.
- `REJECTED` has a stored rejection reason and no journal entry or balance effect.
- `REVERSED` has a separate posted reversing journal entry. The original transfer and journal history remain immutable.
- Client idempotency is mandatory for `POST`; a repeated matching request returns the original transfer.

## 4. Accounting rules

For an amount of `N` atomic APX:

| Ledger account | Direction | Amount |
| --- | --- | --- |
| Source wallet's ledger account | Debit | `N` |
| Destination wallet's ledger account | Credit | `N` |

The source and destination wallet IDs must differ, both wallets must be `ACTIVE`, and `N > 0`. Available balance is derived from posted ledger entries. The database transaction must prevent two simultaneous requests from both spending the same available balance; use row-level locking on the source ledger account or a serializable transaction with retry handling.

## 5. API contract

`POST /api/v1/transfers`

```json
{
  "sourceWalletId": "9dca4c11-526d-47ce-838b-f82ea6b38edf",
  "destinationWalletId": "4b2f0c32-914b-4d72-b1f0-93e9a4e2455c",
  "amountAtomic": 125000000,
  "idempotencyKey": "transfer-20260809-001"
}
```

Successful response: `201 Created`

```json
{
  "id": "68a9fdea-0101-4fc8-aa7a-ba341d0209c1",
  "sourceWalletId": "9dca4c11-526d-47ce-838b-f82ea6b38edf",
  "destinationWalletId": "4b2f0c32-914b-4d72-b1f0-93e9a4e2455c",
  "amountAtomic": 125000000,
  "status": "POSTED",
  "journalEntryId": "f6bd65fb-a752-4d0e-907b-bb5c74e73c9a",
  "createdAt": "2026-08-09T15:30:00Z",
  "postedAt": "2026-08-09T15:30:01Z"
}
```

Additional reads:

- `GET /api/v1/transfers/{transferId}`
- `GET /api/v1/wallets/{walletId}/transfers`

## 6. Error responses

| Situation | HTTP status | Code |
| --- | ---: | --- |
| Source and destination are the same | 400 | `SAME_WALLET_TRANSFER` |
| Invalid UUID, missing key, or invalid amount | 400 | `VALIDATION_ERROR` or `INVALID_ATOMIC_AMOUNT` |
| Wallet does not exist | 404 | `WALLET_NOT_FOUND` |
| Source or destination is frozen | 409 | `WALLET_NOT_ACTIVE` |
| Source balance is insufficient | 409 | `INSUFFICIENT_FUNDS` |
| Key reused with different request | 409 | `IDEMPOTENCY_KEY_CONFLICT` |
| Transfer cannot be reversed | 409 | `TRANSFER_NOT_REVERSIBLE` |

## 7. Persistence design

`transfers` stores UUIDs for source/destination wallets, atomic amount (`BIGINT`), lifecycle status, unique idempotency key, immutable created/post/reversal timestamps, journal entry references, and optional rejection reason. Add checks for positive amount and distinct wallets, foreign keys to wallets and journals, and indexes by source, destination, and creation time.

The transfer record, journal posting, and status transition are one transaction. If any validation or posting step fails, the transaction rolls back and no wallet balance changes.

## 8. Acceptance criteria

- A successful transfer posts exactly one balanced journal with the source debit and destination credit in atomic APX units.
- No endpoint updates a wallet balance column.
- Insufficient-funds checks are correct under concurrent requests and never allow the source balance below zero.
- A repeated matching idempotency key produces no additional journal entry or balance change.
- Frozen, missing, same, or invalid wallets cannot be used for a posted transfer.
- Reversal produces a new compensating posted journal; it does not edit the original transfer or journal lines.
- Unit, PostgreSQL integration, and concurrency tests verify accounting and idempotency invariants.

## 9. Follow-up

After transfer correctness is established, emit a `TransferPosted` domain event. The private blockchain-style settlement feature can group those confirmed transfers into blocks without becoming the source of financial truth.
