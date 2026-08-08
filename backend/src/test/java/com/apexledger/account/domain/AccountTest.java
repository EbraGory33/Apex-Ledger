package com.apexledger.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountTest {
    private static final UUID ACCOUNT_UUID = UUID.fromString("72f667ef-0953-4de2-8d75-87ee5194bdc2");
    private static final Instant CREATED_AT = Instant.parse("2026-08-06T15:30:00Z");
    private static final Clock CREATED_CLOCK = Clock.fixed(CREATED_AT, ZoneOffset.UTC);

    @Test
    void createsActiveAccountWithNormalizedDisplayName() {
        Account account = Account.create("  Ada Lovelace  ", CREATED_CLOCK, () -> ACCOUNT_UUID);

        assertThat(account.id().value()).isEqualTo(ACCOUNT_UUID);
        assertThat(account.displayName()).isEqualTo("Ada Lovelace");
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.createdAt()).isEqualTo(CREATED_AT);
        assertThat(account.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void rejectsBlankDisplayName() {
        assertThatThrownBy(() -> Account.create("   ", CREATED_CLOCK, () -> ACCOUNT_UUID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("displayName must contain 1 to 120 non-whitespace characters");
    }

    @Test
    void rejectsOversizedDisplayName() {
        assertThatThrownBy(() -> Account.create("a".repeat(121), CREATED_CLOCK, () -> ACCOUNT_UUID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("displayName must contain 1 to 120 non-whitespace characters");
    }

    @Test
    void changesStatusAndUpdatedAtWhenStatusChanges() {
        Account account = Account.create("Ada Lovelace", CREATED_CLOCK, () -> ACCOUNT_UUID);
        Instant suspendedAt = Instant.parse("2026-08-06T16:30:00Z");

        account.changeStatus(AccountStatus.SUSPENDED, Clock.fixed(suspendedAt, ZoneOffset.UTC));

        assertThat(account.status()).isEqualTo(AccountStatus.SUSPENDED);
        assertThat(account.createdAt()).isEqualTo(CREATED_AT);
        assertThat(account.updatedAt()).isEqualTo(suspendedAt);
    }

    @Test
    void repeatedStatusChangeIsIdempotentAndKeepsUpdatedAt() {
        Account account = Account.create("Ada Lovelace", CREATED_CLOCK, () -> ACCOUNT_UUID);

        account.changeStatus(AccountStatus.ACTIVE, Clock.fixed(Instant.parse("2026-08-06T17:30:00Z"), ZoneOffset.UTC));

        assertThat(account.updatedAt()).isEqualTo(CREATED_AT);
    }
}
