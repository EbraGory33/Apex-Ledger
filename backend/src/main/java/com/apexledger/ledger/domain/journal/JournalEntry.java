package com.apexledger.ledger.domain.journal;

import java.time.Instant;
import java.util.List;

import com.apexledger.ledger.domain.ledger.EntryDirection;
import com.apexledger.ledger.domain.ledger.LedgerEntry;
import com.apexledger.ledger.domain.ledger.LedgerEntryIdGenerator;

public final class JournalEntry {

    private final JournalEntryId id;
    private final String referenceType;
    private final String referenceId;
    private final List<LedgerEntry> entries;
    private final Instant createdAt;
    private JournalEntryStatus status;
    private Instant postedAt;
    private JournalEntryId reversalOfId;

    private JournalEntry(JournalEntryId id, String referenceType, String referenceId, List<LedgerEntry> entries,
            JournalEntryStatus status, Instant createdAt, Instant postedAt, JournalEntryId reversalOfId) {
        if (id == null || status == null || createdAt == null) {
            throw new IllegalArgumentException("journal entry fields are required");
        }
        this.id = id;
        this.referenceType = normalizeReferenceType(referenceType);
        this.referenceId = referenceId;
        this.entries = List.copyOf(entries == null ? List.of() : entries);
        this.status = status;
        this.createdAt = createdAt;
        this.postedAt = postedAt;
        this.reversalOfId = reversalOfId;
    }

    public static JournalEntry draft(String referenceType, String referenceId, List<LedgerEntry> entries,
            Instant now, JournalEntryIdGenerator generator) {
        return new JournalEntry(JournalEntryId.generate(generator), referenceType, referenceId, entries,
                JournalEntryStatus.DRAFT, now, null, null);
    }

    public static JournalEntry reconstitute(JournalEntryId id, String referenceType, String referenceId,
            List<LedgerEntry> entries, JournalEntryStatus status, Instant createdAt, Instant postedAt, JournalEntryId reversalOfId) {
        return new JournalEntry(id, referenceType, referenceId, entries, status, createdAt, postedAt, reversalOfId);
    }

    public void post(Instant now) {
        if (status == JournalEntryStatus.POSTED) {
            return;
        }
        if (status != JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("journal entry cannot be posted");
        }
        if (entries.size() < 2 || !isBalanced()) {
            throw new JournalEntryNotBalancedException();
        }
        status = JournalEntryStatus.POSTED;
        postedAt = now;
    }

    public JournalEntry reversal(Instant now, JournalEntryIdGenerator journalGenerator, LedgerEntryIdGenerator entryGenerator) {
        if (status != JournalEntryStatus.POSTED || reversalOfId != null) {
            throw new IllegalStateException("journal entry cannot be reversed");
        }
        List<LedgerEntry> reversalEntries = entries.stream().map(entry -> entry.reverse(entryGenerator, now)).toList();
        JournalEntry reversal = new JournalEntry(JournalEntryId.generate(journalGenerator), "REVERSAL", id.value().toString(),
                reversalEntries, JournalEntryStatus.DRAFT, now, null, id);
        reversal.post(now);
        status = JournalEntryStatus.REVERSED;
        return reversal;
    }

    public boolean isBalanced() {
        try {
            long debits = entries.stream().filter(entry -> entry.direction() == EntryDirection.DEBIT)
                    .mapToLong(LedgerEntry::amountAtomic).reduce(0, Math::addExact);
            long credits = entries.stream().filter(entry -> entry.direction() == EntryDirection.CREDIT)
                    .mapToLong(LedgerEntry::amountAtomic).reduce(0, Math::addExact);
            return debits == credits;
        } catch (ArithmeticException exception) {
            return false;
        }
    }

    public JournalEntryId id() {
        return id;
    }

    public String referenceType() {
        return referenceType;
    }

    public String referenceId() {
        return referenceId;
    }

    public List<LedgerEntry> entries() {
        return entries;
    }

    public JournalEntryStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant postedAt() {
        return postedAt;
    }

    public JournalEntryId reversalOfId() {
        return reversalOfId;
    }

    private static String normalizeReferenceType(String type) {
        if (type == null || type.trim().isEmpty() || type.trim().length() > 40) {
            throw new IllegalArgumentException("referenceType is required");
        }
        return type.trim();
    }
}
