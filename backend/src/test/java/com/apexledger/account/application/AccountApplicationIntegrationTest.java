package com.apexledger.account.application;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class AccountApplicationIntegrationTest {

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
    private CreateAccount createAccount;

    @Autowired
    private GetAccount getAccount;

    @Autowired
    private ChangeAccountStatus changeAccountStatus;

    @Test
    void createsAccountAndPersistsIt() {

        Account created = createAccount.create("Ada Lovelace");

        Account retrieved
                = getAccount.get(created.id());

        assertThat(retrieved.id())
                .isEqualTo(created.id());

        assertThat(retrieved.displayName())
                .isEqualTo("Ada Lovelace");

        assertThat(retrieved.status())
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(retrieved.createdAt())
                .isEqualTo(created.createdAt());

        assertThat(retrieved.updatedAt())
                .isEqualTo(created.updatedAt());
    }

    @Test
    void changesAccountStatusAndPersistsIt() {

        Account created
                = createAccount.create("Grace Hopper");

        Account changed
                = changeAccountStatus.change(
                        created.id(),
                        AccountStatus.SUSPENDED
                );

        assertThat(changed.status())
                .isEqualTo(AccountStatus.SUSPENDED);

        Account retrieved
                = getAccount.get(created.id());

        assertThat(retrieved.status())
                .isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    void changesUpdatedAtWhenStatusChanges() {

        Account created
                = createAccount.create("Alan Turing");

        Instant originalUpdatedAt
                = created.updatedAt();

        Account changed
                = changeAccountStatus.change(
                        created.id(),
                        AccountStatus.SUSPENDED
                );

        assertThat(changed.updatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);

        Account retrieved
                = getAccount.get(created.id());

        assertThat(retrieved.updatedAt())
                .isEqualTo(changed.updatedAt());
    }

    @Test
    void keepsUpdatedAtWhenStatusChangeIsIdempotent() {

        Account created
                = createAccount.create("Katherine Johnson");

        Account suspended
                = changeAccountStatus.change(
                        created.id(),
                        AccountStatus.SUSPENDED
                );

        Instant suspendedAt
                = suspended.updatedAt();

        Account repeated
                = changeAccountStatus.change(
                        created.id(),
                        AccountStatus.SUSPENDED
                );

        assertThat(repeated.status())
                .isEqualTo(AccountStatus.SUSPENDED);

        assertThat(repeated.updatedAt())
                .isEqualTo(suspendedAt);

        Account retrieved
                = getAccount.get(created.id());

        assertThat(retrieved.updatedAt())
                .isEqualTo(suspendedAt);
    }

    @Test
    void preservesCreatedAtWhenStatusChanges() {

        Account created
                = createAccount.create("Mary Jackson");

        Instant createdAt
                = created.createdAt();

        Account changed
                = changeAccountStatus.change(
                        created.id(),
                        AccountStatus.SUSPENDED
                );

        assertThat(changed.createdAt())
                .isEqualTo(createdAt);

        Account retrieved
                = getAccount.get(created.id());

        assertThat(retrieved.createdAt())
                .isEqualTo(createdAt);
    }

    @Test
    void throwsWhenChangingStatusOfMissingAccount() {

        AccountId missingId
                = new AccountId(UUID.randomUUID());

        assertThatThrownBy(()
                -> changeAccountStatus.change(
                        missingId,
                        AccountStatus.SUSPENDED
                ))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getThrowsWhenAccountDoesNotExist() {

        AccountId missingId
                = new AccountId(UUID.randomUUID());

        assertThatThrownBy(()
                -> getAccount.get(missingId))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
