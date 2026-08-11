package com.apexledger.ledger.domain.journal;

import java.util.Optional;

import com.apexledger.ledger.domain.ledger.LedgerAccountId;

public interface JournalEntryRepository {

    JournalEntry save(JournalEntry journalEntry);

    Optional<JournalEntry> findById(JournalEntryId id);

    long balanceAtomic(LedgerAccountId ledgerAccountId);
}
