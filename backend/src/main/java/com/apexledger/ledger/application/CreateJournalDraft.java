package com.apexledger.ledger.application;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import com.apexledger.ledger.domain.journal.JournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntryIdGenerator;
import com.apexledger.ledger.domain.journal.JournalEntryRepository;
import com.apexledger.ledger.domain.ledger.EntryDirection;
import com.apexledger.ledger.domain.ledger.LedgerAccountId;
import com.apexledger.ledger.domain.ledger.LedgerEntry;
import com.apexledger.ledger.domain.ledger.LedgerEntryId;
import com.apexledger.ledger.domain.ledger.LedgerEntryIdGenerator;

import jakarta.transaction.Transactional;

@Service
public class CreateJournalDraft {

    public record Line(LedgerAccountId ledgerAccountId, EntryDirection direction, long amountAtomic) {

    }
    private final JournalEntryRepository journals;
    private final Clock clock;
    private final JournalEntryIdGenerator journalIds;
    private final LedgerEntryIdGenerator entryIds;

    public CreateJournalDraft(JournalEntryRepository journals, Clock clock, JournalEntryIdGenerator journalIds, LedgerEntryIdGenerator entryIds) {
        this.journals = journals;
        this.clock = clock;
        this.journalIds = journalIds;
        this.entryIds = entryIds;
    }

    @Transactional
    public JournalEntry create(String referenceType, String referenceId, List<Line> lines) {
        var now = clock.instant();
        List<LedgerEntry> entries = lines.stream().map(line -> new LedgerEntry(LedgerEntryId.generate(entryIds),
                line.ledgerAccountId(), line.direction(), line.amountAtomic(), now)).toList();
        return journals.save(JournalEntry.draft(referenceType, referenceId, entries, now, journalIds));
    }
}
