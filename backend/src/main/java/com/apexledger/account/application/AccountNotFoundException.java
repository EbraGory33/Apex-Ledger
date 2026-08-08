package com.apexledger.account.application;

import com.apexledger.account.domain.AccountId;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId accountId) {
        super("Account not found: " + accountId);
    }
}
