package com.apexledger.ledger.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_accounts")
public class LedgerAccountEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "wallet_id", nullable = false, unique = true)
    private UUID walletId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerAccountEntity() {
    }

    LedgerAccountEntity(UUID id, UUID walletId, Instant createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    UUID getWalletId() {
        return walletId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
