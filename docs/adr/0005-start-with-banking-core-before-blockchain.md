# ADR 0005: Start With the Banking Core Before Blockchain

## Status

Accepted

## Context

ApexLedger has a long-term vision that includes blockchain-inspired settlement, block history, exchange simulation, portfolio analytics, and fraud detection. However, the first major goal is to build a credible banking core.

Blockchain features depend on correct accounting and transaction history. If the ledger and transfer model are weak, adding blocks and hashes will not make the system financially sound.

## Decision

ApexLedger will build the banking core before blockchain functionality.

The initial domain sequence is:

```text
Account
Wallet
Ledger
Transfer
```

Blockchain settlement will be added later as a layer over confirmed ledger-backed transfers.

## Rationale

The ledger should be the financial source of truth. Blockchain-inspired components should provide settlement history, verification, and explorer-style visibility, but they should not replace core accounting.

This keeps the project focused on financial correctness before distributed-system simulation.

## Consequences

### Benefits

- Stronger financial foundation.
- Cleaner accounting model.
- More realistic fintech architecture.
- Blockchain layer can consume confirmed transfer events later.
- Easier to test early workflows.

### Tradeoffs

- Blockchain features are delayed.
- Early demos may look less flashy.
- More effort goes into accounting fundamentals first.

## Evolution Path

1. Create accounts.
2. Create wallets.
3. Create ledger accounts.
4. Post journal entries.
5. Support wallet-to-wallet APX transfers.
6. Emit transfer-confirmed events.
7. Group confirmed transfers into private blockchain-style blocks.
8. Expose block explorer APIs.
