package com.example.java.resilience4j;


import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class RetryTest {


    void callMe() {
        log.info("Try Call me");
        throw new IllegalArgumentException("Ups error");
    }

    @Test
    void createNewRetry() {
// memakai runnable untuk memanggil method yg tidak memiliki return value
        Retry retry = Retry.ofDefaults("ZIP");

        Runnable runnable = Retry.decorateRunnable(retry, () -> callMe());

        runnable.run();

    }

    String hello() {
        log.info("Call say Hello");
        throw new IllegalArgumentException("Ups error say hello");
    }

    @Test
    void createRetrySupplier() {
        Retry retry = Retry.ofDefaults("ZIP");
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> hello());
        supplier.get();
    }

    //  membuat konfigurasi retry sendiri, jika retry1 / retry2 tidak di deklarasikan config nya maka akan memakai ofDefault
    @Test
    void testRetryRegistryConfig() {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(2))
                .build();

        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        retryRegistry.addConfiguration("config", config);

        Retry retry1 = retryRegistry.retry("HEHE", config);
        Retry retry2 = retryRegistry.retry("HEHE");

        Assertions.assertSame(retry1, retry2);

        Runnable runnable = Retry.decorateRunnable(retry1, () -> callMe());
        runnable.run();

    }

}
