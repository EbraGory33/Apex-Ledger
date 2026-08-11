package com.apexledger.ledger.domain.ledger;

import java.util.UUID;

public record LedgerAccountId(UUID value) {

    public LedgerAccountId {
        if (value == null) {
            throw new IllegalArgumentException("ledger account id is required");
    
        }}

    public static LedgerAccountId generate(LedgerAccountIdGenerator generator) {
        return new LedgerAccountId(generator.generate());
    }
}
