package com.apexledger.wallet.domain;

import java.util.UUID;

@FunctionalInterface
public interface WalletIdGenerator {
    UUID generate();
}
