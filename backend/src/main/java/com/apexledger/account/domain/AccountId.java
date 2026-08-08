package com.apexledger.account.domain;

import java.util.UUID;

public record AccountId(UUID value) {
    public AccountId {
        if (value == null) {
            throw new IllegalArgumentException("account id is required");
        }
    }

    public static AccountId generate(AccountIdGenerator generator) {
        return new AccountId(generator.generate());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
