package com.example.java.resilience4j;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TimeLimiterTest {

    @SneakyThrows
    public String slow() {

        Thread.sleep(5000L);
        return "slow";
    }

    @Test
    public void testTimeLimiter() throws Exception {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slow());

        TimeLimiter timeLimiter = TimeLimiter.ofDefaults("default");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);
        callable.call();
    }

    @Test
    public void testTimeLimiterConfig() throws Exception {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slow());

        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        TimeLimiter timeLimiter = TimeLimiter.of("default", config);
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);
        callable.call();
    }

}
