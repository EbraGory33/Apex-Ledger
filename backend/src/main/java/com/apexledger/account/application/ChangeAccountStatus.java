package com.apexledger.account.application;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountRepository;
import com.apexledger.account.domain.AccountStatus;
import java.time.Clock;
import org.springframework.stereotype.Service;

@Service
public class ChangeAccountStatus {
    private final AccountRepository repository;
    private final Clock clock;

    public ChangeAccountStatus(AccountRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Account change(AccountId accountId, AccountStatus status) {
        Account account = repository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        account.changeStatus(status, clock);
        return repository.save(account);
    }
}
