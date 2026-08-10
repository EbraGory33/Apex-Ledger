package com.apexledger.account.application;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountIdGenerator;
import com.apexledger.account.domain.AccountRepository;

import jakarta.transaction.Transactional;

@Service
public class CreateAccount {

    private final AccountRepository repository;
    private final Clock clock;
    private final AccountIdGenerator idGenerator;

    public CreateAccount(AccountRepository repository, Clock clock, AccountIdGenerator idGenerator) {
        this.repository = repository;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Account create(String displayName) {
        return repository.save(Account.create(displayName, clock, idGenerator));
    }
}
