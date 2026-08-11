package com.apexledger.ledger.domain.journal;

public class JournalEntryNotBalancedException extends RuntimeException {

    public JournalEntryNotBalancedException() {
        super("journal entry must contain balanced debit and credit totals");
    }
}
