package com.apexledger.ledger.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.apexledger.ledger.domain.ledger.LedgerAccount;
import com.apexledger.ledger.domain.ledger.LedgerAccountId;
import com.apexledger.ledger.domain.ledger.LedgerAccountRepository;
import com.apexledger.wallet.domain.WalletId;

import jakarta.persistence.EntityManager;

@Repository
public class PostgresLedgerAccountRepository implements LedgerAccountRepository {

    private final EntityManager entityManager;

    public PostgresLedgerAccountRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public LedgerAccount save(LedgerAccount account) {
        if (entityManager.find(LedgerAccountEntity.class, account.id().value()) == null) {
            entityManager.persist(new LedgerAccountEntity(account.id().value(), account.walletId().value(), account.createdAt()));
        }
        return account;
    }

    @Override
    public Optional<LedgerAccount> findByWalletId(WalletId walletId) {
        return entityManager.createQuery("select a from LedgerAccountEntity a where a.walletId = :walletId", LedgerAccountEntity.class)
                .setParameter("walletId", walletId.value()).getResultStream().findFirst().map(this::toDomain);
    }

    @Override
    public Optional<LedgerAccount> findById(LedgerAccountId id) {
        return Optional.ofNullable(entityManager.find(LedgerAccountEntity.class, id.value())).map(this::toDomain);
    }

    private LedgerAccount toDomain(LedgerAccountEntity entity) {
        return new LedgerAccount(new LedgerAccountId(entity.getId()), new WalletId(entity.getWalletId()), entity.getCreatedAt());
    }
}
