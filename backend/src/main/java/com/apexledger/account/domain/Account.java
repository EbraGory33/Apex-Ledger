package com.apexledger.account.domain;

import java.time.Clock;
import java.time.Instant;

public final class Account {

    private static final int MAX_DISPLAY_NAME_LENGTH = 120;

    private final AccountId id;
    private final String displayName;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Account(AccountId id, String displayName, AccountStatus status, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("account id is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("account status is required");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("account timestamps are required");
        }
        this.id = id;
        this.displayName = normalizeDisplayName(displayName);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(String displayName, Clock clock, AccountIdGenerator idGenerator) {
        if (clock == null) {
            throw new IllegalArgumentException("clock is required");
        }
        Instant now = clock.instant();
        return new Account(AccountId.generate(idGenerator), displayName, AccountStatus.ACTIVE, now, now);
    }

    public static Account reconstitute(
            AccountId id,
            String displayName,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Account(id, displayName, status, createdAt, updatedAt);
    }

    public Account changeStatus(AccountStatus requestedStatus, Clock clock) {
        if (requestedStatus == null) {
            throw new IllegalArgumentException("account status is required");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock is required");
        }
        if (status != requestedStatus) {
            status = requestedStatus;
            updatedAt = clock.instant();
        }
        return this;
    }

    public Account copy() {
        return new Account(id, displayName, status, createdAt, updatedAt);
    }

    public AccountId id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public AccountStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String normalizeDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("displayName must contain 1 to 120 non-whitespace characters");
        }
        String normalized = displayName.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("displayName must contain 1 to 120 non-whitespace characters");
        }
        return normalized;
    }
}
