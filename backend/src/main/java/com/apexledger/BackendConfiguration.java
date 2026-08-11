package com.apexledger;

import java.time.Clock;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apexledger.account.domain.AccountIdGenerator;
import com.apexledger.ledger.domain.journal.JournalEntryIdGenerator;
import com.apexledger.ledger.domain.ledger.LedgerAccountIdGenerator;
import com.apexledger.ledger.domain.ledger.LedgerEntryIdGenerator;
import com.apexledger.wallet.domain.WalletIdGenerator;

@Configuration
public class BackendConfiguration {

    @Bean
    @SuppressWarnings("unused")
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @SuppressWarnings("unused")
    AccountIdGenerator accountIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    @SuppressWarnings("unused")
    WalletIdGenerator walletIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    @SuppressWarnings("unused")
    LedgerAccountIdGenerator ledgerAccountIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    @SuppressWarnings("unused")
    JournalEntryIdGenerator journalEntryIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    @SuppressWarnings("unused")
    LedgerEntryIdGenerator ledgerEntryIdGenerator() {
        return UUID::randomUUID;
    }
}
