package com.apexledger.wallet.application;

import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;

public interface AccountStatusLookup {
    AccountStatus getStatus(AccountId accountId);
}
