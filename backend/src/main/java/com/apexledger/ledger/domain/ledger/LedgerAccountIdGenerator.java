package com.apexledger.ledger.domain.ledger;

import java.util.UUID;

@FunctionalInterface
public interface LedgerAccountIdGenerator {

    UUID generate();
}
