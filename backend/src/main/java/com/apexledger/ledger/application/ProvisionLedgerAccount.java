package com.apexledger.ledger.application;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.apexledger.ledger.domain.ledger.LedgerAccount;
import com.apexledger.ledger.domain.ledger.LedgerAccountIdGenerator;
import com.apexledger.ledger.domain.ledger.LedgerAccountRepository;
import com.apexledger.wallet.application.WalletNotFoundException;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletRepository;

import jakarta.transaction.Transactional;

@Service
public class ProvisionLedgerAccount {

    private final LedgerAccountRepository ledgerAccounts;
    private final WalletRepository wallets;
    private final Clock clock;
    private final LedgerAccountIdGenerator idGenerator;

    public ProvisionLedgerAccount(LedgerAccountRepository ledgerAccounts, WalletRepository wallets, Clock clock,
            LedgerAccountIdGenerator idGenerator) {
        this.ledgerAccounts = ledgerAccounts;
        this.wallets = wallets;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public LedgerAccount provision(WalletId walletId) {
        wallets.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
        return ledgerAccounts.findByWalletId(walletId)
                .orElseGet(() -> ledgerAccounts.save(LedgerAccount.create(walletId, clock.instant(), idGenerator)));
    }
}
