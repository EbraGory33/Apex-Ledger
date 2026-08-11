package com.apexledger.wallet.application;

import com.apexledger.account.domain.AccountId;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(AccountId accountId) {
        super("Account is not active: " + accountId);
    }
}
