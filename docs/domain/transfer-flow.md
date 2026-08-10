```mermaid
flowchart TD

    A[Transfer Requested]

    A --> B[Validate Source Wallet]
    B --> C[Validate Destination Wallet]
    C --> D[Validate Available Balance]

    D --> E[Create Transfer Record]

    E --> F[Create JournalEntry]

    F --> G[Debit Source LedgerAccount]
    F --> H[Credit Destination LedgerAccount]

    G --> I[LedgerEntries]
    H --> I

    I --> J[Balanced JournalEntry]

    J --> K[LedgerAccount Balances]

    K --> L[Wallet Balance Derived From Ledger]
```
