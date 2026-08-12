# Feature Design: Ledger Account and Journal Entry Lifecycle

**Status:** Proposed  
**Feature:** F-003 — Ledger account and journal entry lifecycle  
**Owner:** ApexLedger backend

## 1. Purpose

Make the ledger the authoritative record of asset movement. APX is the first supported asset, while the accounting model deliberately identifies the asset on every ledger account so real currencies can be added without changing account identity. This feature creates the structures that later issuance and transfers post to; it does not expose a mutable wallet balance.

## 2. Scope

### In scope

- Define supported assets and their atomic-unit precision. Seed APX with eight decimal places.
- Provision exactly one ledger account for each `(wallet, asset)` pair. Wallet creation provisions its APX ledger account in the same application transaction.
- Support wallet-owned and non-wallet-owned system ledger accounts.
- Create a journal entry in `DRAFT` state with two or more immutable debit/credit lines.
- Validate and post a journal entry that balances independently for every asset represented by its lines.
- Reverse a posted entry by posting a separate, equal-and-opposite journal entry.
- Derive a ledger-account balance from posted entries, expressed in that account's atomic units.

### Out of scope

- APX issuance policy and end-user funding; these are defined in F-004.
- Wallet-to-wallet transfer requests; these are defined in F-005.
- Cross-asset journals, foreign-exchange conversion, exchange rates, fees, and holds. Adding real currencies to the asset catalog is planned, but F-003 initially enables APX only.
- Editing or deleting a posted entry, changing a ledger line, or storing a mutable wallet balance.

## 3. Domain model and invariants

| Entity | Essential fields | Lifecycle |
| --- | --- | --- |
| `Asset` | `code`, `name`, `decimalPlaces` | Supported accounting denomination; APX is initially active. |
| `LedgerAccount` | `id`, `accountType`, `walletId?`, `systemAccountCode?`, `assetCode`, `createdAt` | Created once per wallet/asset or system-account identity; never deleted. |
| `JournalEntry` | `id`, `referenceType`, `referenceId`, `status`, `createdAt`, `postedAt`, `reversalOfId` | `DRAFT → POSTED → REVERSED`. |
| `LedgerEntry` | `id`, `journalEntryId`, `ledgerAccountId`, `direction`, `amountAtomic`, `createdAt` | Created with a draft; immutable forever. |

- `LedgerAccount.accountType` is `WALLET` or `SYSTEM`. A `WALLET` account requires `walletId` and forbids `systemAccountCode`; a `SYSTEM` account requires `systemAccountCode` and forbids `walletId`.
- A wallet account is unique by `(walletId, assetCode)`, and provisioning is idempotent for that pair. A wallet may therefore hold one account per supported asset in the future.
- A system account is unique by `(systemAccountCode, assetCode)`, allowing explicit identities such as `APX_ISSUANCE` without relying on a magic UUID.
- `assetCode` must reference an existing row in `assets`; arbitrary strings are not valid assets. Asset codes use 3–10 uppercase ASCII letters, and `decimalPlaces` is between 0 and 18.
- `amountAtomic` is a positive signed 64-bit integer in the ledger account's asset. APX initially has eight decimal places, so one APX equals `100,000,000` atomic units. No `double`, `float`, or decimal amount is permitted in domain or persistence code.
- A journal entry contains at least two lines. Each line obtains its asset from its ledger account; callers cannot supply a conflicting asset on a line.
- When posting, `sum(DEBIT amounts) == sum(CREDIT amounts)` exactly **for each asset**. Numeric equality between different assets never makes a journal balanced.
- A posted entry and its lines cannot be updated or deleted. Corrections use a new posted reversal entry whose lines swap debit/credit direction while preserving amounts.
- A reversal can target a posted entry only once. The original entry is marked `REVERSED` only after its reversal is posted successfully.
- A ledger-account balance is derived as total credits minus total debits across every financially posted entry, including an original entry later marked `REVERSED` and its posted reversal. Their equal-and-opposite lines therefore net to zero without erasing history.

## 4. Module shape

```text
com.apexledger.ledger
├── domain
│   ├── Asset / AssetCode / AssetRepository
│   ├── LedgerAccount / LedgerAccountId / LedgerAccountType / LedgerAccountRepository
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

Wallet creation invokes ledger-account provisioning through a published ledger application contract. The orchestration commits wallet creation and initial APX provisioning together, establishing that every new wallet is immediately usable by ledger-backed workflows. Ledger owns ledger accounts and the asset catalog and may not write wallet tables. Existing wallets are backfilled during the migration; the internal provisioning command remains idempotent for repair/bootstrap use.

## 5. API contract

The first public read endpoint remains APX-specific while APX is the only enabled asset:

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

Use four Flyway tables (UUID primary keys except for the asset code):

- `assets`: constrained code primary key, name, and `decimal_places`; seed `APX / Apex / 8`.
- `ledger_accounts`: account type, mutually exclusive wallet/system identity, asset foreign key, timestamp, ownership-shape check, and unique wallet/asset and system-code/asset identities.
- `journal_entries`: status, reference type/ID, creation/posting timestamps, nullable `reversal_of_id`, and a unique partial index preventing more than one reversal per original entry.
- `ledger_entries`: journal-entry and ledger-account foreign keys, `direction`, `amount_atomic BIGINT`, timestamp, and `CHECK (amount_atomic > 0)`.

Database constraints enforce supported asset identity, account ownership shape, referential integrity, uniqueness, and positive atomic amounts. Application code resolves each line's account and enforces balanced totals per asset before posting. Posting, line persistence, and state change occur in one database transaction.

## 7. Failure handling

| Situation | Result |
| --- | --- |
| Missing wallet | `404 WALLET_NOT_FOUND` |
| Unknown/disabled asset | `400 ASSET_NOT_SUPPORTED` |
| No APX ledger account for wallet balance request | `404 LEDGER_ACCOUNT_NOT_FOUND` (indicates failed provisioning/backfill, not a normal wallet state) |
| Draft has fewer than two lines or is unbalanced | `422 JOURNAL_ENTRY_NOT_BALANCED` |
| Attempt to edit/delete a posted entry | `409 JOURNAL_ENTRY_IMMUTABLE` |
| Post an already posted entry | idempotent success; no duplicate lines or balance effect |
| Reverse a non-posted or previously reversed entry | `409 JOURNAL_ENTRY_NOT_REVERSIBLE` |
| Atomic amount is zero, negative, or overflows | `400 INVALID_ATOMIC_AMOUNT` |

## 8. Acceptance criteria

- The asset catalog rejects malformed or unregistered codes and records APX with eight decimal places.
- Creating a wallet also creates its APX wallet ledger account atomically.
- Provisioning a ledger account twice for the same wallet/asset returns the same account and creates one database row.
- A system ledger account can exist without a wallet; a wallet account cannot.
- A journal entry cannot post unless debit and credit totals are equal independently for every represented asset.
- A journal containing equal numeric APX and USD lines is rejected rather than treated as balanced.
- Every persisted ledger line has a positive integer atomic amount and a valid debit/credit direction.
- Posting is atomic: a failed post changes neither journal status nor any balance projection.
- A ledger balance changes only because of posted lines, never from a wallet column.
- Reversal produces a new, balanced, posted journal entry and leaves historical lines intact.
- Unit tests prove balance, immutability, and reversal invariants; PostgreSQL tests prove transaction and uniqueness constraints.

## 9. Delivery slices

1. Atomic amount/value objects and ledger domain invariants.
2. Asset catalog plus typed ledger-account schema, automatic APX wallet provisioning, system accounts, and zero-balance read.
3. Draft/post/reversal journal workflow with persistence and tests.
4. Read-model optimization only after correct ledger queries are measured.
