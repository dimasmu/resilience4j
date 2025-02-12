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
public class RetryConfigTest {

    String hello() {
        log.info("Call hello()");
        throw new IllegalArgumentException("Ups");
//        return "hello";
    }

    @Test
    void retryConfig() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(IllegalArgumentException.class) // akan selalu attempt sesuai jumlah maxAttempt jika bertemu illegalArgumentException
//                .ignoreExceptions(RuntimeException.class) // jika di method hello ada runtime exception maka akan langsung return error tanpa attempt
                .build();

        Retry retry = Retry.of("HEHE", config);

        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> hello());
        supplier.get();
    }

    @Test
    void testRetryRegistry() {

        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

        Retry retry1 = retryRegistry.retry("HEHE");
        Retry retry2 = retryRegistry.retry("HEHE");

        Assertions.assertSame(retry1, retry2);
    }

}
