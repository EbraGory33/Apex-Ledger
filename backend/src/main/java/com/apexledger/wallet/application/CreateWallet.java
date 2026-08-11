package com.apexledger.wallet.application;

import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletIdGenerator;
import com.apexledger.wallet.domain.WalletRepository;
import jakarta.transaction.Transactional;
import java.time.Clock;
import org.springframework.stereotype.Service;

@Service
public class CreateWallet {
    private final WalletRepository repository;
    private final AccountStatusLookup accountStatusLookup;
    private final Clock clock;
    private final WalletIdGenerator idGenerator;

    public CreateWallet(WalletRepository repository, AccountStatusLookup accountStatusLookup,
            Clock clock, WalletIdGenerator idGenerator) {
        this.repository = repository;
        this.accountStatusLookup = accountStatusLookup;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Wallet create(AccountId accountId, String label) {
        if (accountStatusLookup.getStatus(accountId) != AccountStatus.ACTIVE) {
            throw new InactiveAccountException(accountId);
        }
        String normalizedLabel = Wallet.normalizeLabel(label);
        if (repository.existsByAccountIdAndNormalizedLabel(accountId, normalizedLabel)) {
            throw new DuplicateWalletLabelException(accountId, normalizedLabel);
        }
        return repository.save(Wallet.create(accountId, normalizedLabel, clock, idGenerator));
    }
}
