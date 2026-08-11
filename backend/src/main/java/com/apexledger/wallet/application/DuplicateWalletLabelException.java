package com.apexledger.wallet.application;

import com.apexledger.account.domain.AccountId;

public class DuplicateWalletLabelException extends RuntimeException {
    public DuplicateWalletLabelException(AccountId accountId, String label) {
        super("Wallet label already exists for account " + accountId + ": " + label);
    }
}
