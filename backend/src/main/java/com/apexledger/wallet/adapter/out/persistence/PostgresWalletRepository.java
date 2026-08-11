package com.apexledger.wallet.adapter.out.persistence;

import com.apexledger.account.domain.AccountId;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresWalletRepository implements WalletRepository {
    private final EntityManager entityManager;
    public PostgresWalletRepository(EntityManager entityManager) { this.entityManager = entityManager; }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity existing = entityManager.find(WalletEntity.class, wallet.id().value());
        if (existing == null) {
            entityManager.persist(new WalletEntity(wallet.id().value(), wallet.accountId().value(), wallet.label(),
                    wallet.assetCode(), wallet.status(), wallet.createdAt(), wallet.updatedAt()));
        } else {
            existing.setStatus(wallet.status());
            existing.setUpdatedAt(wallet.updatedAt());
        }
        return wallet.copy();
    }

    @Override
    public Optional<Wallet> findById(WalletId id) {
        return Optional.ofNullable(entityManager.find(WalletEntity.class, id.value())).map(this::toDomain);
    }

    @Override
    public List<Wallet> findByAccountId(AccountId accountId) {
        return entityManager.createQuery("select w from WalletEntity w where w.accountId = :accountId order by w.createdAt", WalletEntity.class)
                .setParameter("accountId", accountId.value())
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByAccountIdAndNormalizedLabel(AccountId accountId, String normalizedLabel) {
        Long count = entityManager.createQuery(
                        "select count(w) from WalletEntity w where w.accountId = :accountId and lower(w.label) = :label", Long.class)
                .setParameter("accountId", accountId.value())
                .setParameter("label", normalizedLabel.toLowerCase(Locale.ROOT))
                .getSingleResult();
        return count > 0;
    }

    private Wallet toDomain(WalletEntity entity) {
        return Wallet.reconstitute(new WalletId(entity.getId()), new AccountId(entity.getAccountId()), entity.getLabel(),
                entity.getAssetCode(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
