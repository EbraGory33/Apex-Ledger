package com.apexledger.ledger.domain.journal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.apexledger.ledger.domain.ledger.EntryDirection;
import com.apexledger.ledger.domain.ledger.LedgerAccountId;
import com.apexledger.ledger.domain.ledger.LedgerEntry;
import com.apexledger.ledger.domain.ledger.LedgerEntryId;

class JournalEntryTest {

    private static final Instant NOW = Instant.parse("2026-08-10T21:00:00Z");
    private static final LedgerAccountId DEBIT_ACCOUNT = new LedgerAccountId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    private static final LedgerAccountId CREDIT_ACCOUNT = new LedgerAccountId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    @Test
    void postsBalancedDraftAndKeepsPostIdempotent() {
        JournalEntry journal = draft(100);

        journal.post(NOW);
        journal.post(Instant.parse("2026-08-10T21:01:00Z"));

        assertThat(journal.status()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(journal.postedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsUnbalancedDraft() {
        JournalEntry journal = JournalEntry.draft("MANUAL", "reference", List.of(
                entry(DEBIT_ACCOUNT, EntryDirection.DEBIT, 100), entry(CREDIT_ACCOUNT, EntryDirection.CREDIT, 99)), NOW, UUID::randomUUID);

        assertThatThrownBy(() -> journal.post(NOW)).isInstanceOf(JournalEntryNotBalancedException.class);
        assertThat(journal.status()).isEqualTo(JournalEntryStatus.DRAFT);
    }

    @Test
    void createsPostedOppositeReversalWithoutChangingHistoricalLines() {
        JournalEntry original = draft(100);
        original.post(NOW);

        JournalEntry reversal = original.reversal(Instant.parse("2026-08-10T21:01:00Z"), UUID::randomUUID, UUID::randomUUID);

        assertThat(original.status()).isEqualTo(JournalEntryStatus.REVERSED);
        assertThat(reversal.status()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(reversal.reversalOfId()).isEqualTo(original.id());
        assertThat(original.entries()).allSatisfy(entry -> assertThat(entry.amountAtomic()).isEqualTo(100));
        assertThat(reversal.entries()).extracting(LedgerEntry::direction)
                .containsExactly(EntryDirection.CREDIT, EntryDirection.DEBIT);
    }

    @Test
    void rejectsSecondReversal() {
        JournalEntry original = draft(100);
        original.post(NOW);
        original.reversal(NOW, UUID::randomUUID, UUID::randomUUID);

        assertThatThrownBy(() -> original.reversal(NOW, UUID::randomUUID, UUID::randomUUID))
                .isInstanceOf(IllegalStateException.class);
    }

    private JournalEntry draft(long amount) {
        return JournalEntry.draft("MANUAL", "reference", List.of(
                entry(DEBIT_ACCOUNT, EntryDirection.DEBIT, amount), entry(CREDIT_ACCOUNT, EntryDirection.CREDIT, amount)), NOW, UUID::randomUUID);
    }

    private LedgerEntry entry(LedgerAccountId accountId, EntryDirection direction, long amount) {
        return new LedgerEntry(new LedgerEntryId(UUID.randomUUID()), accountId, direction, amount, NOW);
    }
}
