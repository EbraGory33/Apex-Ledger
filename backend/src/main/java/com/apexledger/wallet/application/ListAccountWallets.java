package com.apexledger.wallet.application;

import com.apexledger.account.domain.AccountId;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListAccountWallets {
    private final WalletRepository repository;
    private final AccountStatusLookup accountStatusLookup;
    public ListAccountWallets(WalletRepository repository, AccountStatusLookup accountStatusLookup) {
        this.repository = repository;
        this.accountStatusLookup = accountStatusLookup;
    }
    public List<Wallet> list(AccountId accountId) {
        accountStatusLookup.getStatus(accountId);
        return repository.findByAccountId(accountId);
    }
}
