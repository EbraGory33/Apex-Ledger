package com.apexledger.wallet.application;

import com.apexledger.wallet.domain.WalletId;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(WalletId walletId) {
        super("Wallet not found: " + walletId);
    }
}
