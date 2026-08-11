package com.apexledger.ledger.domain.ledger;

import java.util.Optional;

import com.apexledger.wallet.domain.WalletId;

public interface LedgerAccountRepository {

    LedgerAccount save(LedgerAccount account);

    Optional<LedgerAccount> findByWalletId(WalletId walletId);

    Optional<LedgerAccount> findById(LedgerAccountId id);
}
