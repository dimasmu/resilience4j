package com.example.java.resilience4j;

import io.github.resilience4j.bulkhead.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

// bulkhead bertugas untuk menjaga jumlah eksekusi concurrent
// jika bulkhead penuh maka dia akan mengembalikan error ketika program menerima eksekusi lebih dari jumlah concurency yg diminta oleh bulkhead
@Slf4j
public class BulkHeadTest {

    private AtomicLong counter = new AtomicLong(0L);

    @SneakyThrows
    public void slow() {
        long value = counter.incrementAndGet();
        log.info("slow: " + value);
        Thread.sleep(5000L);
    }

    //Semaphore bulkhead secara default hanya menerima maksimal 25 concurency setiap kali looping lalu setelah penuh maka akan return error (BulkheadFullException)
    @Test
    void testSemaphore() throws InterruptedException {

        Bulkhead bulkhead = Bulkhead.ofDefaults("default");

        for (int i = 0; i < 1000; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    //threadpool bulkhead secara default menerima maksimal concurency berdasarkan ketersediaan thread processor device masing masing user lalu setelah penuh maka akan return error (BulkheadFullException)
    @Test
    void testThreadPool() throws InterruptedException {
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));
        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.ofDefaults("default");

        for (int i = 0; i < 1000; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();

        }
    }

    @Test
    void testSemaphoreConfig() throws InterruptedException {

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();

        Bulkhead bulkhead = Bulkhead.of("default", config);

        for (int i = 0; i < 10; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testThreadPoolConfig() throws InterruptedException {
        ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                .queueCapacity(100) // default 100 jika tidak di deklarasi
                .build();

        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.ofDefaults("default");

        for (int i = 0; i < 20; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();

        }

        Thread.sleep(10_000L);
    }

    @Test
    void testSemaphoreRegistry() throws InterruptedException {

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();

        Bulkhead bulkhead = registry.bulkhead("default", config);

        for (int i = 0; i < 10; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testThreadPoolRegistry() throws InterruptedException {

        ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                .queueCapacity(100) // default 100 jika tidak di deklarasi
                .build();

        ThreadPoolBulkheadRegistry registry = ThreadPoolBulkheadRegistry.ofDefaults();
        registry.addConfiguration("config", config);

        ThreadPoolBulkhead bulkhead = registry.bulkhead("default", config);

        for (int i = 0; i < 20; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();

        }

        Thread.sleep(10_000L);

    }

}
