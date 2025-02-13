package com.example.java.resilience4j;

import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

public class MetricTest {

    private String sayHello() {
        throw new IllegalArgumentException("Ups");
    }

    @Test
    void retry() {

        Retry retry = Retry.ofDefaults("default");

        try {
            Supplier<String> supplier = Retry.decorateSupplier(retry, () -> sayHello());
            supplier.get();
        } catch (Exception e) {
            System.out.println(retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
        }

    }

}
