package com.apexledger.wallet.adapter.out.persistence;

import com.apexledger.wallet.domain.WalletStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class WalletEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    @Column(name = "label", nullable = false, length = 80)
    private String label;
    @Column(name = "asset_code", nullable = false, length = 10)
    private String assetCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WalletStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WalletEntity() { }

    public WalletEntity(UUID id, UUID accountId, String label, String assetCode,
            WalletStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.label = label;
        this.assetCode = assetCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public String getLabel() { return label; }
    public String getAssetCode() { return assetCode; }
    public WalletStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setStatus(WalletStatus status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
