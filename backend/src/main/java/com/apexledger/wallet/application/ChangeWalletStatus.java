package com.apexledger.wallet.application;

import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletRepository;
import com.apexledger.wallet.domain.WalletStatus;
import jakarta.transaction.Transactional;
import java.time.Clock;
import org.springframework.stereotype.Service;

@Service
public class ChangeWalletStatus {
    private final WalletRepository repository;
    private final Clock clock;
    public ChangeWalletStatus(WalletRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }
    @Transactional
    public Wallet change(WalletId walletId, WalletStatus status) {
        Wallet wallet = repository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.changeStatus(status, clock);
        return repository.save(wallet);
    }
}
