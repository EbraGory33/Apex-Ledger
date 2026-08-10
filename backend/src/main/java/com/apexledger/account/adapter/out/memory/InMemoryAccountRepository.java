package com.apexledger.account.adapter.out.memory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountRepository;

/**
 * Concurrency-safe development repository. This adapter is intentionally
 * non-durable and must not be used as the financial source of truth.
 */
@Repository
@Profile("in-memory")
public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentMap<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        accounts.put(account.id(), account.copy());
        return account.copy();
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(accounts.get(id)).map(Account::copy);
    }
}
