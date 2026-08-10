package com.apexledger.account.adapter.out.persistence;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
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

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostgresAccountRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgresAccountRepository repository;

    @Test
    void savesAndRetrievesAccount() {

        UUID id = UUID.randomUUID();

        Instant createdAt
                = Instant.parse("2026-08-06T15:30:00Z");

        Clock clock
                = Clock.fixed(createdAt, ZoneOffset.UTC);

        Account account = Account.create(
                "Ada Lovelace",
                clock,
                () -> id
        );

        repository.save(account);

        Account result = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(result.id().value())
                .isEqualTo(id);

        assertThat(result.displayName())
                .isEqualTo("Ada Lovelace");

        assertThat(result.status())
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(result.createdAt())
                .isEqualTo(createdAt);

        assertThat(result.updatedAt())
                .isEqualTo(createdAt);
    }

    @Test
    void returnsEmptyWhenAccountDoesNotExist() {

        UUID id = UUID.randomUUID();

        Optional<Account> result
                = repository.findById(new AccountId(id));

        assertThat(result).isEmpty();
    }

    @Test
    void updatesExistingAccount() {

        UUID id = UUID.randomUUID();

        Instant createdAt
                = Instant.parse("2026-08-06T15:30:00Z");

        Instant suspendedAt
                = Instant.parse("2026-08-06T16:30:00Z");

        Account account = Account.create(
                "Ada Lovelace",
                Clock.fixed(createdAt, ZoneOffset.UTC),
                () -> id
        );

        repository.save(account);

        account.changeStatus(
                AccountStatus.SUSPENDED,
                Clock.fixed(suspendedAt, ZoneOffset.UTC)
        );

        repository.save(account);

        Account result = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(result.id().value())
                .isEqualTo(id);

        assertThat(result.displayName())
                .isEqualTo("Ada Lovelace");

        assertThat(result.status())
                .isEqualTo(AccountStatus.SUSPENDED);

        assertThat(result.createdAt())
                .isEqualTo(createdAt);

        assertThat(result.updatedAt())
                .isEqualTo(suspendedAt);
    }

    @Test
    void keepsCreatedAtWhenUpdatingAccount() {

        UUID id = UUID.randomUUID();

        Instant createdAt
                = Instant.parse("2026-08-06T15:30:00Z");

        Instant suspendedAt
                = Instant.parse("2026-08-06T16:30:00Z");

        Account account = Account.create(
                "Grace Hopper",
                Clock.fixed(createdAt, ZoneOffset.UTC),
                () -> id
        );

        repository.save(account);

        account.changeStatus(
                AccountStatus.SUSPENDED,
                Clock.fixed(suspendedAt, ZoneOffset.UTC)
        );

        repository.save(account);

        Account result = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(result.createdAt())
                .isEqualTo(createdAt);

        assertThat(result.updatedAt())
                .isEqualTo(suspendedAt);
    }

    @Test
    void idempotentStatusChangeDoesNotUpdateUpdatedAt() {

        UUID id = UUID.randomUUID();

        Instant createdAt
                = Instant.parse("2026-08-06T15:30:00Z");

        Instant suspendedAt
                = Instant.parse("2026-08-06T16:30:00Z");

        Instant repeatedChangeAt
                = Instant.parse("2026-08-06T17:30:00Z");

        Account account = Account.create(
                "Grace Hopper",
                Clock.fixed(createdAt, ZoneOffset.UTC),
                () -> id
        );

        repository.save(account);

        account.changeStatus(
                AccountStatus.SUSPENDED,
                Clock.fixed(suspendedAt, ZoneOffset.UTC)
        );

        repository.save(account);

        Account suspendedAccount = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(suspendedAccount.status())
                .isEqualTo(AccountStatus.SUSPENDED);

        assertThat(suspendedAccount.updatedAt())
                .isEqualTo(suspendedAt);

        account.changeStatus(
                AccountStatus.SUSPENDED,
                Clock.fixed(repeatedChangeAt, ZoneOffset.UTC)
        );

        repository.save(account);

        Account result = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(result.status())
                .isEqualTo(AccountStatus.SUSPENDED);

        assertThat(result.updatedAt())
                .isEqualTo(suspendedAt);
    }

    @Test
    void preservesDisplayNameWhenUpdatingStatus() {

        UUID id = UUID.randomUUID();

        Instant createdAt
                = Instant.parse("2026-08-06T15:30:00Z");

        Instant suspendedAt
                = Instant.parse("2026-08-06T16:30:00Z");

        Account account = Account.create(
                "  Ada Lovelace  ",
                Clock.fixed(createdAt, ZoneOffset.UTC),
                () -> id
        );

        repository.save(account);

        account.changeStatus(
                AccountStatus.SUSPENDED,
                Clock.fixed(suspendedAt, ZoneOffset.UTC)
        );

        repository.save(account);

        Account result = repository
                .findById(new AccountId(id))
                .orElseThrow();

        assertThat(result.displayName())
                .isEqualTo("Ada Lovelace");

        assertThat(result.status())
                .isEqualTo(AccountStatus.SUSPENDED);
    }
}
