package com.apexledger.ledger.adapter.out.persistence;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import com.apexledger.ledger.domain.ledger.EntryDirection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "journal_entry_id", nullable = false)
    @SuppressWarnings("unused")
    private UUID journalEntryId;
    @Column(name = "ledger_account_id", nullable = false)
    private UUID ledgerAccountId;
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private EntryDirection direction;
    @Column(name = "amount_atomic", nullable = false)
    private BigInteger amountAtomic;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntryEntity() {
    }

    LedgerEntryEntity(UUID id, UUID journalId, UUID accountId, EntryDirection direction, BigInteger amount, Instant createdAt) {
        this.id = id;
        this.journalEntryId = journalId;
        this.ledgerAccountId = accountId;
        this.direction = direction;
        this.amountAtomic = amount;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    EntryDirection getDirection() {
        return direction;
    }

    BigInteger getAmountAtomic() {
        return amountAtomic;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
