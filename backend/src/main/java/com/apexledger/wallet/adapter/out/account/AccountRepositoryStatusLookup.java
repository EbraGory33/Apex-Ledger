package com.apexledger.wallet.adapter.out.account;

import com.apexledger.account.application.AccountNotFoundException;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountRepository;
import com.apexledger.account.domain.AccountStatus;
import com.apexledger.wallet.application.AccountStatusLookup;
import org.springframework.stereotype.Component;

@Component
public class AccountRepositoryStatusLookup implements AccountStatusLookup {
    private final AccountRepository accountRepository;
    public AccountRepositoryStatusLookup(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Override
    public AccountStatus getStatus(AccountId accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .status();
    }
}
