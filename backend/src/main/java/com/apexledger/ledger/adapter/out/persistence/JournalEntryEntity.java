package com.apexledger.ledger.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import com.apexledger.ledger.domain.journal.JournalEntryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_entries")
public class JournalEntryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "reference_type", nullable = false)
    private String referenceType;
    @Column(name = "reference_id")
    private String referenceId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JournalEntryStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "posted_at")
    private Instant postedAt;
    @Column(name = "reversal_of_id", unique = true)
    private UUID reversalOfId;

    protected JournalEntryEntity() {
    }

    JournalEntryEntity(UUID id, String type, String referenceId, JournalEntryStatus status, Instant createdAt, Instant postedAt, UUID reversalOfId) {
        this.id = id;
        this.referenceType = type;
        this.referenceId = referenceId;
        this.status = status;
        this.createdAt = createdAt;
        this.postedAt = postedAt;
        this.reversalOfId = reversalOfId;
    }

    UUID getId() {
        return id;
    }

    String getReferenceType() {
        return referenceType;
    }

    String getReferenceId() {
        return referenceId;
    }

    JournalEntryStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getPostedAt() {
        return postedAt;
    }

    UUID getReversalOfId() {
        return reversalOfId;
    }

    void setStatus(JournalEntryStatus status) {
        this.status = status;
    }

    void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }
}
