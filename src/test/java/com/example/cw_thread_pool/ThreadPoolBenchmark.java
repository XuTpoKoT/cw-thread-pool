package com.example.cw_thread_pool;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.*;

@Fork(value = 1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ThreadPoolBenchmark {

    private ExecutorService jdkPool;
    private CustomThreadPool customPool;
    private static final int TASKS_PER_RUN = 1000;

    @Setup(Level.Trial)
    public void setup() {
        jdkPool = new ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100_000));
        customPool = new CustomThreadPool(4, 4, 100_000, 60, TimeUnit.SECONDS, "custom", Logger.disabled());
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        jdkPool.shutdown();
        customPool.shutdown();
    }

    @Benchmark
    public void jdkThreadPoolBenchmark() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(TASKS_PER_RUN);
        for (int i = 0; i < TASKS_PER_RUN; i++) {
            jdkPool.execute(() -> latch.countDown());
        }
        latch.await();
    }

    @Benchmark
    public void customThreadPoolBenchmark() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(TASKS_PER_RUN);
        for (int i = 0; i < TASKS_PER_RUN; i++) {
            customPool.execute(() -> latch.countDown());
        }
        latch.await();
    }
}
