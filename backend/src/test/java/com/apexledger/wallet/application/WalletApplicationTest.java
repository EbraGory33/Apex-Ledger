package com.apexledger.wallet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletRepository;
import com.apexledger.wallet.domain.WalletStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletApplicationTest {
    private static final AccountId ACCOUNT_ID = new AccountId(UUID.fromString("72f667ef-0953-4de2-8d75-87ee5194bdc2"));
    private static final WalletId WALLET_ID = new WalletId(UUID.fromString("9dca4c11-526d-47ce-838b-f82ea6b38edf"));
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-10T15:30:00Z"), ZoneOffset.UTC);

    @Test
    void createsNormalizedActiveWalletForAnActiveAccount() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();
        FixedAccountStatusLookup accounts = new FixedAccountStatusLookup(AccountStatus.ACTIVE);
        CreateWallet service = new CreateWallet(repository, accounts, CLOCK, () -> WALLET_ID.value());

        Wallet created = service.create(ACCOUNT_ID, "  Primary wallet  ");

        assertThat(created.id()).isEqualTo(WALLET_ID);
        assertThat(created.label()).isEqualTo("Primary wallet");
        assertThat(created.status()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(repository.findById(WALLET_ID)).hasValueSatisfying(saved -> {
            assertThat(saved.id()).isEqualTo(created.id());
            assertThat(saved.label()).isEqualTo(created.label());
        });
    }

    @Test
    void rejectsCreationForAnInactiveAccount() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();
        CreateWallet service = new CreateWallet(repository, new FixedAccountStatusLookup(AccountStatus.SUSPENDED), CLOCK,
                () -> WALLET_ID.value());

        assertThatThrownBy(() -> service.create(ACCOUNT_ID, "Primary wallet"))
                .isInstanceOf(InactiveAccountException.class);
        assertThat(repository.findByAccountId(ACCOUNT_ID)).isEmpty();
    }

    @Test
    void rejectsDuplicateLabelsCaseInsensitively() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();
        repository.save(Wallet.create(ACCOUNT_ID, "Primary wallet", CLOCK, () -> WALLET_ID.value()));
        CreateWallet service = new CreateWallet(repository, new FixedAccountStatusLookup(AccountStatus.ACTIVE), CLOCK,
                UUID::randomUUID);

        assertThatThrownBy(() -> service.create(ACCOUNT_ID, "  PRIMARY WALLET  "))
                .isInstanceOf(DuplicateWalletLabelException.class);
    }

    @Test
    void listsWalletsForAnExistingAccount() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();
        Wallet wallet = Wallet.create(ACCOUNT_ID, "Primary", CLOCK, () -> WALLET_ID.value());
        repository.save(wallet);
        ListAccountWallets service = new ListAccountWallets(repository, new FixedAccountStatusLookup(AccountStatus.ACTIVE));

        assertThat(service.list(ACCOUNT_ID)).singleElement().satisfies(result -> {
            assertThat(result.id()).isEqualTo(wallet.id());
            assertThat(result.label()).isEqualTo(wallet.label());
        });
    }

    @Test
    void changesWalletStatusAndPersistsIt() {
        InMemoryWalletRepository repository = new InMemoryWalletRepository();
        Wallet wallet = Wallet.create(ACCOUNT_ID, "Primary", CLOCK, () -> WALLET_ID.value());
        repository.save(wallet);
        ChangeWalletStatus service = new ChangeWalletStatus(repository,
                Clock.fixed(Instant.parse("2026-08-10T15:31:00Z"), ZoneOffset.UTC));

        Wallet changed = service.change(WALLET_ID, WalletStatus.FROZEN);

        assertThat(changed.status()).isEqualTo(WalletStatus.FROZEN);
        assertThat(repository.findById(WALLET_ID)).hasValueSatisfying(saved ->
                assertThat(saved.updatedAt()).isEqualTo(Instant.parse("2026-08-10T15:31:00Z")));
    }

    static final class FixedAccountStatusLookup implements AccountStatusLookup {
        private final AccountStatus status;
        FixedAccountStatusLookup(AccountStatus status) { this.status = status; }
        @Override public AccountStatus getStatus(AccountId accountId) { return status; }
    }

    static final class InMemoryWalletRepository implements WalletRepository {
        private final Map<WalletId, Wallet> wallets = new HashMap<>();
        @Override public Wallet save(Wallet wallet) { wallets.put(wallet.id(), wallet.copy()); return wallet.copy(); }
        @Override public Optional<Wallet> findById(WalletId id) { return Optional.ofNullable(wallets.get(id)).map(Wallet::copy); }
        @Override public List<Wallet> findByAccountId(AccountId accountId) {
            return wallets.values().stream().filter(wallet -> wallet.accountId().equals(accountId)).map(Wallet::copy).toList();
        }
        @Override public boolean existsByAccountIdAndNormalizedLabel(AccountId accountId, String label) {
            String normalized = label.toLowerCase(Locale.ROOT);
            return wallets.values().stream().anyMatch(wallet -> wallet.accountId().equals(accountId)
                    && wallet.label().toLowerCase(Locale.ROOT).equals(normalized));
        }
    }
}
