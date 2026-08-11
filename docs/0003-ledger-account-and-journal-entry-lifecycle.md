# Feature Design: Ledger Account and Journal Entry Lifecycle

**Status:** Proposed  
**Feature:** F-003 — Ledger account and journal entry lifecycle  
**Owner:** ApexLedger backend

## 1. Purpose

Make the ledger the authoritative record of APX movement. This feature creates the accounting structures that later issuance and transfers post to; it does not expose a mutable wallet balance.

## 2. Scope

### In scope

- Provision exactly one ledger account for each wallet.
- Create a journal entry in `DRAFT` state with two or more immutable debit/credit lines.
- Validate and post a balanced APX journal entry atomically.
- Reverse a posted entry by posting a separate, equal-and-opposite journal entry.
- Derive a ledger-account balance from posted entries, expressed only in APX atomic units.

### Out of scope

- APX issuance policy and end-user funding; these are defined in F-004.
- Wallet-to-wallet transfer requests; these are defined in F-005.
- Multi-currency accounting, exchange rates, fees, and holds.
- Editing or deleting a posted entry, changing a ledger line, or storing a mutable wallet balance.

## 3. Domain model and invariants

| Entity | Essential fields | Lifecycle |
| --- | --- | --- |
| `LedgerAccount` | `id`, `walletId`, `createdAt` | Created once per wallet; never deleted. |
| `JournalEntry` | `id`, `referenceType`, `referenceId`, `status`, `createdAt`, `postedAt`, `reversalOfId` | `DRAFT → POSTED → REVERSED`. |
| `LedgerEntry` | `id`, `journalEntryId`, `ledgerAccountId`, `direction`, `amountAtomic`, `createdAt` | Created with a draft; immutable forever. |

- A `LedgerAccount` has a unique `walletId`; creation is idempotent for the same wallet.
- `amountAtomic` is a positive signed 64-bit integer. One APX equals `100,000,000` atomic units. No `double`, `float`, or decimal amount is permitted in domain or persistence code.
- A journal entry contains at least two lines and one currency (`APX`).
- When posting, `sum(DEBIT amounts) == sum(CREDIT amounts)` exactly.
- A posted entry and its lines cannot be updated or deleted. Corrections use a new posted reversal entry whose lines swap debit/credit direction while preserving amounts.
- A reversal can target a posted entry only once. The original entry is marked `REVERSED` only after its reversal is posted successfully.
- A ledger-account balance is derived as total credits minus total debits across every financially posted entry, including an original entry later marked `REVERSED` and its posted reversal. Their equal-and-opposite lines therefore net to zero without erasing history.

## 4. Module shape

```text
com.apexledger.ledger
├── domain
│   ├── LedgerAccount / LedgerAccountId / LedgerAccountRepository
│   ├── JournalEntry / JournalEntryId / JournalEntryRepository
│   ├── LedgerEntry / EntryDirection / AtomicAmount
│   └── JournalEntryStatus
├── application
│   ├── ProvisionLedgerAccount
│   ├── CreateJournalDraft
│   ├── PostJournalEntry
│   ├── ReverseJournalEntry
│   └── GetLedgerAccountBalance
└── adapter
    ├── in.web
    └── out.persistence
```

Wallet requests ledger-account provisioning through a published ledger application contract. Ledger owns the mapping and may not write to wallet tables beyond reading the wallet identifier.

## 5. API contract

The first public read endpoint is:

`GET /api/v1/wallets/{walletId}/balance`

```json
{
  "walletId": "9dca4c11-526d-47ce-838b-f82ea6b38edf",
  "assetCode": "APX",
  "balanceAtomic": 0
}
```

Posting and reversal endpoints are internal/admin operations until authorization is designed:

- `POST /api/v1/internal/journal-entries`
- `POST /api/v1/internal/journal-entries/{journalEntryId}/post`
- `POST /api/v1/internal/journal-entries/{journalEntryId}/reverse`

The post command must return the journal ID, `POSTED` status, and immutable lines. It must never return a calculated balance as the authoritative result of the command.

## 6. Persistence design

Use three Flyway tables with UUID primary keys:

- `ledger_accounts`: unique foreign key `wallet_id`, timestamp.
- `journal_entries`: status, reference type/ID, creation/posting timestamps, nullable `reversal_of_id`, and a unique partial index preventing more than one reversal per original entry.
- `ledger_entries`: journal-entry and ledger-account foreign keys, `direction`, `amount_atomic BIGINT`, timestamp, and `CHECK (amount_atomic > 0)`.

Database constraints enforce referential integrity and positive atomic amounts. Application code enforces balanced totals before posting. Posting, line persistence, and state change occur in one database transaction.

## 7. Failure handling

| Situation | Result |
| --- | --- |
| Missing wallet | `404 WALLET_NOT_FOUND` |
| No ledger account for wallet balance request | `404 LEDGER_ACCOUNT_NOT_FOUND` |
| Draft has fewer than two lines or is unbalanced | `422 JOURNAL_ENTRY_NOT_BALANCED` |
| Attempt to edit/delete a posted entry | `409 JOURNAL_ENTRY_IMMUTABLE` |
| Post an already posted entry | idempotent success; no duplicate lines or balance effect |
| Reverse a non-posted or previously reversed entry | `409 JOURNAL_ENTRY_NOT_REVERSIBLE` |
| Atomic amount is zero, negative, or overflows | `400 INVALID_ATOMIC_AMOUNT` |

## 8. Acceptance criteria

- Provisioning a ledger account twice for a wallet returns the same account and creates one database row.
- A journal entry cannot post unless its APX debit and credit totals are equal.
- Every persisted ledger line has a positive integer atomic amount and a valid debit/credit direction.
- Posting is atomic: a failed post changes neither journal status nor any balance projection.
- A ledger balance changes only because of posted lines, never from a wallet column.
- Reversal produces a new, balanced, posted journal entry and leaves historical lines intact.
- Unit tests prove balance, immutability, and reversal invariants; PostgreSQL tests prove transaction and uniqueness constraints.

## 9. Delivery slices

1. Atomic amount/value objects and ledger domain invariants.
2. Ledger-account schema, provisioning, and zero-balance read.
3. Draft/post/reversal journal workflow with persistence and tests.
4. Read-model optimization only after correct ledger queries are measured.
