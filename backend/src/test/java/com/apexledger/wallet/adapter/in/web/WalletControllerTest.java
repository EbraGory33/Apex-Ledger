package com.apexledger.wallet.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;
import com.apexledger.shared.web.ApiExceptionHandler;
import com.apexledger.wallet.application.AccountStatusLookup;
import com.apexledger.wallet.application.ChangeWalletStatus;
import com.apexledger.wallet.application.CreateWallet;
import com.apexledger.wallet.application.GetWallet;
import com.apexledger.wallet.application.ListAccountWallets;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WalletControllerTest {
    private static final UUID ACCOUNT_UUID = UUID.fromString("72f667ef-0953-4de2-8d75-87ee5194bdc2");
    private static final UUID WALLET_UUID = UUID.fromString("9dca4c11-526d-47ce-838b-f82ea6b38edf");
    private final InMemoryWalletRepository repository = new InMemoryWalletRepository();
    private final AccountStatusLookup accountStatusLookup = accountId -> AccountStatus.ACTIVE;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-10T15:30:00Z"), ZoneOffset.UTC);
        CreateWallet createWallet = new CreateWallet(repository, accountStatusLookup, clock, () -> WALLET_UUID);
        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(createWallet, new GetWallet(repository),
                        new ListAccountWallets(repository, accountStatusLookup), new ChangeWalletStatus(repository, clock)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createsWalletAndReturnsLocation() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{accountId}/wallets", ACCOUNT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"  Primary wallet  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/wallets/" + WALLET_UUID))
                .andExpect(jsonPath("$.id").value(WALLET_UUID.toString()))
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_UUID.toString()))
                .andExpect(jsonPath("$.label").value("Primary wallet"))
                .andExpect(jsonPath("$.assetCode").value("APX"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void listsWalletsForAccount() throws Exception {
        repository.save(wallet());

        mockMvc.perform(get("/api/v1/accounts/{accountId}/wallets", ACCOUNT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(WALLET_UUID.toString()))
                .andExpect(jsonPath("$.items[0].label").value("Primary wallet"));
    }

    @Test
    void freezesWallet() throws Exception {
        repository.save(wallet());

        mockMvc.perform(put("/api/v1/wallets/{walletId}/status", WALLET_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FROZEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }

    @Test
    void mapsInvalidWalletIdError() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WALLET_ID"));
    }

    private Wallet wallet() {
        return Wallet.reconstitute(new WalletId(WALLET_UUID), new AccountId(ACCOUNT_UUID), "Primary wallet", "APX",
                WalletStatus.ACTIVE, Instant.parse("2026-08-10T15:30:00Z"), Instant.parse("2026-08-10T15:30:00Z"));
    }

    static final class InMemoryWalletRepository implements WalletRepository {
        private final Map<WalletId, Wallet> wallets = new HashMap<>();
        @Override public Wallet save(Wallet wallet) { wallets.put(wallet.id(), wallet.copy()); return wallet.copy(); }
        @Override public Optional<Wallet> findById(WalletId id) { return Optional.ofNullable(wallets.get(id)).map(Wallet::copy); }
        @Override public List<Wallet> findByAccountId(AccountId accountId) {
            return wallets.values().stream().filter(wallet -> wallet.accountId().equals(accountId)).map(Wallet::copy).toList();
        }
        @Override public boolean existsByAccountIdAndNormalizedLabel(AccountId accountId, String label) {
            return wallets.values().stream().anyMatch(wallet -> wallet.accountId().equals(accountId)
                    && wallet.label().equalsIgnoreCase(label.toLowerCase(Locale.ROOT)));
        }
    }
}
