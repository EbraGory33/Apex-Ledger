package com.apexledger.account.domain;

import java.util.UUID;

@FunctionalInterface
public interface AccountIdGenerator {
    UUID generate();
}
