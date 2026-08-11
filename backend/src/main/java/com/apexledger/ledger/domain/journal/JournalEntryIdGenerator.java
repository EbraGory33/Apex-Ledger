package com.apexledger.ledger.domain.journal;

import java.util.UUID;

@FunctionalInterface
public interface JournalEntryIdGenerator {

    UUID generate();
}
