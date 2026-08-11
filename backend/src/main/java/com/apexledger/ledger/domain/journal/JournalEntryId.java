package com.apexledger.ledger.domain.journal;

import java.util.UUID;

public record JournalEntryId(UUID value) {

    public JournalEntryId {
        if (value == null) {
            throw new IllegalArgumentException("journal entry id is required");
    
        }}

    public static JournalEntryId generate(JournalEntryIdGenerator generator) {
        return new JournalEntryId(generator.generate());
    }
}
