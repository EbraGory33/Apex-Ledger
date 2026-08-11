package com.apexledger.wallet.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.apexledger.account.domain.AccountId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletTest {
    private final AccountId accountId = new AccountId(UUID.fromString("72f667ef-0953-4de2-8d75-87ee5194bdc2"));
    private final WalletIdGenerator idGenerator = () -> UUID.fromString("9dca4c11-526d-47ce-838b-f82ea6b38edf");
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-09T15:30:00Z"), ZoneOffset.UTC);

    @Test
    void createsActiveApxWalletWithNormalizedLabel() {
        Wallet wallet = Wallet.create(accountId, "  Primary wallet  ", clock, idGenerator);

        assertThat(wallet.id().value()).isEqualTo(UUID.fromString("9dca4c11-526d-47ce-838b-f82ea6b38edf"));
        assertThat(wallet.accountId()).isEqualTo(accountId);
        assertThat(wallet.label()).isEqualTo("Primary wallet");
        assertThat(wallet.assetCode()).isEqualTo("APX");
        assertThat(wallet.status()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(wallet.createdAt()).isEqualTo(Instant.parse("2026-08-09T15:30:00Z"));
        assertThat(wallet.updatedAt()).isEqualTo(wallet.createdAt());
    }

    @Test
    void rejectsBlankAndOversizedLabels() {
        assertThatThrownBy(() -> Wallet.create(accountId, "   ", clock, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("label must contain 1 to 80 non-whitespace characters");
        assertThatThrownBy(() -> Wallet.create(accountId, "x".repeat(81), clock, idGenerator))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changesUpdatedAtWhenStatusChanges() {
        Wallet wallet = Wallet.create(accountId, "Primary", clock, idGenerator);
        Clock later = Clock.fixed(Instant.parse("2026-08-09T15:31:00Z"), ZoneOffset.UTC);

        wallet.changeStatus(WalletStatus.FROZEN, later);

        assertThat(wallet.status()).isEqualTo(WalletStatus.FROZEN);
        assertThat(wallet.updatedAt()).isEqualTo(Instant.parse("2026-08-09T15:31:00Z"));
        assertThat(wallet.createdAt()).isEqualTo(Instant.parse("2026-08-09T15:30:00Z"));
    }

    @Test
    void keepsUpdatedAtWhenStatusChangeIsIdempotent() {
        Wallet wallet = Wallet.create(accountId, "Primary", clock, idGenerator);

        wallet.changeStatus(WalletStatus.ACTIVE,
                Clock.fixed(Instant.parse("2026-08-09T15:31:00Z"), ZoneOffset.UTC));

        assertThat(wallet.updatedAt()).isEqualTo(Instant.parse("2026-08-09T15:30:00Z"));
    }

    @Test
    void rejectsNonApxAssetWhenReconstituting() {
        assertThatThrownBy(() -> Wallet.reconstitute(new WalletId(idGenerator.generate()), accountId, "Primary", "USD",
                WalletStatus.ACTIVE, Instant.now(), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("assetCode must be APX");
    }
}
