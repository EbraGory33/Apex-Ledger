package com.apexledger.ledger.application;

import com.apexledger.ledger.domain.journal.JournalEntryId;

public class JournalEntryNotFoundException extends RuntimeException {

    public JournalEntryNotFoundException(JournalEntryId id) {
        super("journal entry not found: " + id.value());
    }
}
