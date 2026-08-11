package com.apexledger.ledger.application;
import com.apexledger.wallet.domain.WalletId;
public class LedgerAccountNotFoundException extends RuntimeException {
    public LedgerAccountNotFoundException(WalletId walletId) { super("ledger account not found for wallet: " + walletId.value()); }
}
