package com.apexledger.ledger.domain.ledger;

import java.util.UUID;

public record LedgerEntryId(UUID value) {

    public LedgerEntryId {
        if (value == null) {
            throw new IllegalArgumentException("ledger entry id is required");
    
        }}

    public static LedgerEntryId generate(LedgerEntryIdGenerator generator) {
        return new LedgerEntryId(generator.generate());
    }
}
