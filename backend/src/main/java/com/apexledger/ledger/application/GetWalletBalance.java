package com.apexledger.ledger.application;

import org.springframework.stereotype.Service;

import com.apexledger.ledger.domain.journal.JournalEntryRepository;
import com.apexledger.ledger.domain.ledger.LedgerAccount;
import com.apexledger.ledger.domain.ledger.LedgerAccountRepository;
import com.apexledger.wallet.domain.WalletId;

@Service
public class GetWalletBalance {

    public record Balance(WalletId walletId, long balanceAtomic) {

    }
    private final LedgerAccountRepository ledgerAccounts;
    private final JournalEntryRepository journals;

    public GetWalletBalance(LedgerAccountRepository ledgerAccounts, JournalEntryRepository journals) {
        this.ledgerAccounts = ledgerAccounts;
        this.journals = journals;
    }

    public Balance get(WalletId walletId) {
        LedgerAccount account = ledgerAccounts.findByWalletId(walletId).orElseThrow(() -> new LedgerAccountNotFoundException(walletId));
        return new Balance(walletId, journals.balanceAtomic(account.id()));
    }
}
