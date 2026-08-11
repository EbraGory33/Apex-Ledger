package com.apexledger.wallet.domain;

import java.util.UUID;

public record WalletId(UUID value) {
    public WalletId {
        if (value == null) {
            throw new IllegalArgumentException("wallet id is required");
        }
    }

    public static WalletId generate(WalletIdGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("wallet id generator is required");
        }
        return new WalletId(generator.generate());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
