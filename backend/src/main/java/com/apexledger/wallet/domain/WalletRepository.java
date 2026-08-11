package com.apexledger.wallet.domain;

import com.apexledger.account.domain.AccountId;
import java.util.List;
import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(WalletId id);
    List<Wallet> findByAccountId(AccountId accountId);
    boolean existsByAccountIdAndNormalizedLabel(AccountId accountId, String normalizedLabel);
}
