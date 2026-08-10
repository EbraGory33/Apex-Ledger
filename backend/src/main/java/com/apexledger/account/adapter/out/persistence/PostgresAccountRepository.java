package com.apexledger.account.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountRepository;

import jakarta.persistence.EntityManager;

@Repository
public class PostgresAccountRepository implements AccountRepository {

    private final EntityManager entityManager;

    public PostgresAccountRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Account save(Account account) {

        // System.out.println("ID: " + account.id().value());
        // System.out.println("DISPLAY NAME: " + account.displayName());
        // System.out.println("STATUS: " + account.status().name());
        // System.out.println("CREATED AT: " + account.createdAt());
        // System.out.println("UPDATED AT: " + account.updatedAt());
        AccountEntity existing = entityManager.find(
                AccountEntity.class,
                account.id().value()
        );
        if (existing == null) {
            AccountEntity entity = new AccountEntity(
                    account.id().value(),
                    account.displayName(),
                    account.status(),
                    account.createdAt(),
                    account.updatedAt()
            );
            entityManager.persist(entity);
        } else {
            updateEntity(existing, account);
        }

        return account.copy();
    }

    private void updateEntity(
            AccountEntity entity,
            Account account
    ) {
        entity.setDisplayName(account.displayName());
        entity.setStatus(account.status());
        entity.setUpdatedAt(account.updatedAt());
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        AccountEntity entity
                = entityManager.find(AccountEntity.class, id.value());

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));

    }

    private Account toDomain(AccountEntity entity) {
        return Account.reconstitute(
                new AccountId(entity.getId()),
                entity.getDisplayName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
