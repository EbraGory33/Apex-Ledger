package com.apexledger.ledger.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.apexledger.ledger.domain.journal.JournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntryId;
import com.apexledger.ledger.domain.journal.JournalEntryRepository;
import com.apexledger.ledger.domain.journal.JournalEntryStatus;
import com.apexledger.ledger.domain.ledger.EntryDirection;
import com.apexledger.ledger.domain.ledger.LedgerAccountId;
import com.apexledger.ledger.domain.ledger.LedgerEntry;
import com.apexledger.ledger.domain.ledger.LedgerEntryId;

import jakarta.persistence.EntityManager;

@Repository
public class PostgresJournalEntryRepository implements JournalEntryRepository {

    private final EntityManager entityManager;

    public PostgresJournalEntryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public JournalEntry save(JournalEntry journal) {
        JournalEntryEntity entity = entityManager.find(JournalEntryEntity.class, journal.id().value());
        if (entity == null) {
            entityManager.persist(new JournalEntryEntity(journal.id().value(), journal.referenceType(), journal.referenceId(), journal.status(),
                    journal.createdAt(), journal.postedAt(), journal.reversalOfId() == null ? null : journal.reversalOfId().value()));
            for (LedgerEntry entry : journal.entries()) {
                entityManager.persist(toEntity(journal.id(), entry));
            }
        } else {
            entity.setStatus(journal.status());
            entity.setPostedAt(journal.postedAt());
        }
        return journal;
    }

    @Override
    public Optional<JournalEntry> findById(JournalEntryId id) {
        JournalEntryEntity entity = entityManager.find(JournalEntryEntity.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public long balanceAtomic(LedgerAccountId accountId) {
        Long balance = entityManager.createQuery("select coalesce(sum(case when e.direction = :credit then e.amountAtomic else -e.amountAtomic end), 0) from LedgerEntryEntity e, JournalEntryEntity j where e.journalEntryId = j.id and e.ledgerAccountId = :accountId and j.status in :statuses", Long.class)
                .setParameter("accountId", accountId.value()).setParameter("credit", EntryDirection.CREDIT)
                .setParameter("statuses", List.of(JournalEntryStatus.POSTED, JournalEntryStatus.REVERSED)).getSingleResult();
        return balance;
    }

    private JournalEntry toDomain(JournalEntryEntity entity) {
        List<LedgerEntry> entries = entityManager.createQuery("select e from LedgerEntryEntity e where e.journalEntryId = :journalId order by e.createdAt", LedgerEntryEntity.class)
                .setParameter("journalId", entity.getId()).getResultList().stream().map(this::toDomain).toList();
        return JournalEntry.reconstitute(new JournalEntryId(entity.getId()), entity.getReferenceType(), entity.getReferenceId(), entries,
                entity.getStatus(), entity.getCreatedAt(), entity.getPostedAt(), entity.getReversalOfId() == null ? null : new JournalEntryId(entity.getReversalOfId()));
    }

    private LedgerEntryEntity toEntity(JournalEntryId journalId, LedgerEntry entry) {
        return new LedgerEntryEntity(entry.id().value(), journalId.value(), entry.ledgerAccountId().value(), entry.direction(), entry.amountAtomic(), entry.createdAt());
    }

    private LedgerEntry toDomain(LedgerEntryEntity entity) {
        return new LedgerEntry(new LedgerEntryId(entity.getId()), new LedgerAccountId(entity.getLedgerAccountId()), entity.getDirection(), entity.getAmountAtomic(), entity.getCreatedAt());
    }
}
