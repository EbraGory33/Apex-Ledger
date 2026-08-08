package com.apexledger;

import com.apexledger.account.domain.AccountIdGenerator;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackendConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AccountIdGenerator accountIdGenerator() {
        return UUID::randomUUID;
    }
}
