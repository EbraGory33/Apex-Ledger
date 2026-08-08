package com.apexledger.account.application;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAccount {
    private final AccountRepository repository;

    public GetAccount(AccountRepository repository) {
        this.repository = repository;
    }

    public Account get(AccountId accountId) {
        return repository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
