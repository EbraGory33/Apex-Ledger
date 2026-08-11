package com.apexledger.ledger.application;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.apexledger.ledger.domain.journal.JournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntryId;
import com.apexledger.ledger.domain.journal.JournalEntryRepository;

import jakarta.transaction.Transactional;

@Service
public class PostJournalEntry {

    private final JournalEntryRepository journals;
    private final Clock clock;

    public PostJournalEntry(JournalEntryRepository journals, Clock clock) {
        this.journals = journals;
        this.clock = clock;
    }

    @Transactional
    public JournalEntry post(JournalEntryId id) {
        JournalEntry journal = journals.findById(id).orElseThrow(() -> new JournalEntryNotFoundException(id));
        journal.post(clock.instant());
        return journals.save(journal);
    }
}
