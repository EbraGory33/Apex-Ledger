package com.apexledger.ledger.domain.ledger;

import java.time.Instant;

import com.apexledger.wallet.domain.WalletId;

public record LedgerAccount(LedgerAccountId id, WalletId walletId, Instant createdAt) {

    public LedgerAccount {
        if (id == null || walletId == null || createdAt == null) {
            throw new IllegalArgumentException("ledger account fields are required");
        }
    }

    public static LedgerAccount create(WalletId walletId, Instant now, LedgerAccountIdGenerator generator) {
        return new LedgerAccount(LedgerAccountId.generate(generator), walletId, now);
    }
}
