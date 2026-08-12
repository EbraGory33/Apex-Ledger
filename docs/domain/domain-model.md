```mermaid
classDiagram

    Account "1" --> "0..*" Wallet : owns

    Asset "1" --> "0..*" LedgerAccount : denominates

    Wallet "0..1" --> "0..*" LedgerAccount : owns WALLET accounts

    LedgerAccount "1" --> "0..*" LedgerEntry : contains

    JournalEntry "1" --> "2..*" LedgerEntry : groups

    Transfer "1" --> "1" JournalEntry : causes


    class Account {
        customer profile
    }

    class Wallet {
        product-facing container
    }

    class Asset {
        code
        name
        decimalPlaces
    }

    class LedgerAccount {
        accounting bucket
        accountType WALLET or SYSTEM
        walletId optional
        assetCode
    }

    class JournalEntry {
        balanced accounting event
    }

    class LedgerEntry {
        debit or credit line
    }

    class Transfer {
        requested movement of funds
    }
```
