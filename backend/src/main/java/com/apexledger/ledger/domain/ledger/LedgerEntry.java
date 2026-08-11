package com.apexledger.ledger.domain.ledger;

import java.time.Instant;

public record LedgerEntry(LedgerEntryId id, LedgerAccountId ledgerAccountId, EntryDirection direction,
                          long amountAtomic, Instant createdAt) {
    public LedgerEntry {
        if (id == null || ledgerAccountId == null || direction == null || createdAt == null) {
            throw new IllegalArgumentException("ledger entry fields are required");
        }
        if (amountAtomic <= 0) throw new IllegalArgumentException("amountAtomic must be positive");
    }
    public LedgerEntry reverse(LedgerEntryIdGenerator generator, Instant now) {
        return new LedgerEntry(LedgerEntryId.generate(generator), ledgerAccountId,
                direction == EntryDirection.DEBIT ? EntryDirection.CREDIT : EntryDirection.DEBIT, amountAtomic, now);
    }
}
