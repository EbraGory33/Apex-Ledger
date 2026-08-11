package com.apexledger.wallet.adapter.out.persistence;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.apexledger.account.adapter.out.persistence.PostgresAccountRepository;
import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletStatus;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostgresWalletRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgresAccountRepository accountRepository;
    @Autowired
    private PostgresWalletRepository walletRepository;

    @Test
    void savesRetrievesAndListsWalletsForAnAccount() {
        AccountId accountId = persistAccount();
        WalletId walletId = new WalletId(UUID.randomUUID());
        Instant createdAt = Instant.parse("2026-08-10T15:30:00Z");
        Wallet wallet = Wallet.create(accountId, "  Primary wallet  ",
                Clock.fixed(createdAt, ZoneOffset.UTC), () -> walletId.value());

        walletRepository.save(wallet);

        Wallet result = walletRepository.findById(walletId).orElseThrow();

        assertThat(result.id()).isEqualTo(wallet.id());
        assertThat(result.accountId()).isEqualTo(wallet.accountId());
        assertThat(result.label()).isEqualTo("Primary wallet");
        assertThat(result.assetCode()).isEqualTo("APX");
        assertThat(walletRepository.findByAccountId(result.accountId()))
                .extracting(Wallet::id)
                .containsExactly(result.id());
    }

    @Test
    void findsLabelsCaseInsensitivelyWithinAnAccount() {
        AccountId accountId = persistAccount();
        Wallet wallet = Wallet.create(accountId, "Primary Wallet", Clock.systemUTC(), UUID::randomUUID);
        walletRepository.save(wallet);

        assertThat(walletRepository.existsByAccountIdAndNormalizedLabel(accountId, "primary wallet")).isTrue();
        assertThat(walletRepository.existsByAccountIdAndNormalizedLabel(accountId, "Savings")).isFalse();
    }

    @Test
    void persistsWalletStatusChangesWithoutChangingOwnership() {
        AccountId accountId = persistAccount();
        Instant createdAt = Instant.parse("2026-08-10T15:30:00Z");
        Wallet wallet = Wallet.create(accountId, "Primary", Clock.fixed(createdAt, ZoneOffset.UTC), UUID::randomUUID);
        walletRepository.save(wallet);

        Instant frozenAt = Instant.parse("2026-08-10T15:31:00Z");
        wallet.changeStatus(WalletStatus.FROZEN, Clock.fixed(frozenAt, ZoneOffset.UTC));
        walletRepository.save(wallet);

        Wallet result = walletRepository.findById(wallet.id()).orElseThrow();
        assertThat(result.status()).isEqualTo(WalletStatus.FROZEN);
        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(frozenAt);
    }

    private AccountId persistAccount() {
        UUID accountUuid = UUID.randomUUID();
        Account account = Account.create("Wallet owner", Clock.systemUTC(), () -> accountUuid);
        accountRepository.save(account);
        return account.id();
    }
}
