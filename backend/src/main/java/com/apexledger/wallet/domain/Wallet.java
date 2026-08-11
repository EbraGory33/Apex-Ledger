package com.apexledger.wallet.domain;

import com.apexledger.account.domain.AccountId;
import java.time.Clock;
import java.time.Instant;

public final class Wallet {
    public static final String APX = "APX";
    private static final int MAX_LABEL_LENGTH = 80;

    private final WalletId id;
    private final AccountId accountId;
    private final String label;
    private final String assetCode;
    private WalletStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Wallet(WalletId id, AccountId accountId, String label, String assetCode,
            WalletStatus status, Instant createdAt, Instant updatedAt) {
        if (id == null || accountId == null) {
            throw new IllegalArgumentException("wallet id and account id are required");
        }
        if (!APX.equals(assetCode)) {
            throw new IllegalArgumentException("assetCode must be APX");
        }
        if (status == null || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("wallet status and timestamps are required");
        }
        this.id = id;
        this.accountId = accountId;
        this.label = normalizeLabel(label);
        this.assetCode = assetCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(AccountId accountId, String label, Clock clock, WalletIdGenerator idGenerator) {
        if (clock == null) {
            throw new IllegalArgumentException("clock is required");
        }
        Instant now = clock.instant();
        return new Wallet(WalletId.generate(idGenerator), accountId, label, APX, WalletStatus.ACTIVE, now, now);
    }

    public static Wallet reconstitute(WalletId id, AccountId accountId, String label, String assetCode,
            WalletStatus status, Instant createdAt, Instant updatedAt) {
        return new Wallet(id, accountId, label, assetCode, status, createdAt, updatedAt);
    }

    public Wallet changeStatus(WalletStatus requestedStatus, Clock clock) {
        if (requestedStatus == null || clock == null) {
            throw new IllegalArgumentException("wallet status and clock are required");
        }
        if (status != requestedStatus) {
            status = requestedStatus;
            updatedAt = clock.instant();
        }
        return this;
    }

    public Wallet copy() {
        return new Wallet(id, accountId, label, assetCode, status, createdAt, updatedAt);
    }

    public WalletId id() { return id; }
    public AccountId accountId() { return accountId; }
    public String label() { return label; }
    public String assetCode() { return assetCode; }
    public WalletStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public static String normalizeLabel(String label) {
        if (label == null) {
            throw new IllegalArgumentException("label must contain 1 to 80 non-whitespace characters");
        }
        String normalized = label.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException("label must contain 1 to 80 non-whitespace characters");
        }
        return normalized;
    }
}
