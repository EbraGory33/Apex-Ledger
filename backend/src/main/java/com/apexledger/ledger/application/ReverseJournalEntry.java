package com.apexledger.ledger.application;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.apexledger.ledger.domain.journal.JournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntryId;
import com.apexledger.ledger.domain.journal.JournalEntryIdGenerator;
import com.apexledger.ledger.domain.journal.JournalEntryRepository;
import com.apexledger.ledger.domain.ledger.LedgerEntryIdGenerator;

import jakarta.transaction.Transactional;

@Service
public class ReverseJournalEntry {

    private final JournalEntryRepository journals;
    private final Clock clock;
    private final JournalEntryIdGenerator journalIds;
    private final LedgerEntryIdGenerator entryIds;

    public ReverseJournalEntry(JournalEntryRepository journals, Clock clock, JournalEntryIdGenerator journalIds,
            LedgerEntryIdGenerator entryIds) {
        this.journals = journals;
        this.clock = clock;
        this.journalIds = journalIds;
        this.entryIds = entryIds;
    }

    @Transactional
    public JournalEntry reverse(JournalEntryId id) {
        JournalEntry original = journals.findById(id).orElseThrow(() -> new JournalEntryNotFoundException(id));
        JournalEntry reversal = original.reversal(clock.instant(), journalIds, entryIds);
        journals.save(original);
        return journals.save(reversal);
    }
}
