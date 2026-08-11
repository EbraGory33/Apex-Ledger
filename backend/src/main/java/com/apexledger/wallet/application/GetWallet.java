package com.apexledger.wallet.application;

import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class GetWallet {
    private final WalletRepository repository;
    public GetWallet(WalletRepository repository) { this.repository = repository; }
    public Wallet get(WalletId walletId) {
        return repository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
