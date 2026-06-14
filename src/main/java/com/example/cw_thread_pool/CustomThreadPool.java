package com.example.cw_thread_pool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final Logger logger; // <-- Храним логгер в пуле

    private final List<BlockingQueue<Runnable>> queues;
    private final List<Thread> workers = new ArrayList<>();

    private final AtomicInteger rrIndex = new AtomicInteger(0);
    private final AtomicInteger currentThreads = new AtomicInteger(0);

    private final ThreadFactory threadFactory;

    private volatile boolean isShutdown = false;

    // Конструктор с опциональным логированием
    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int queueSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            String poolName,
            Logger logger
    ) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueSize = queueSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.logger = logger;

        this.threadFactory = new LoggingThreadFactory(poolName, logger);

        this.queues = new ArrayList<>();
        for (int i = 0; i < maxPoolSize; i++) {
            queues.add(new ArrayBlockingQueue<>(queueSize));
        }

        // Создаем core потоки
        for (int i = 0; i < corePoolSize; i++) {
            startWorker();
        }
    }

    private synchronized void startWorker() {
        int id = currentThreads.incrementAndGet();
        String name = "worker-" + id;

        BlockingQueue<Runnable> q = getQueueForWorker();

        // Передаем logger в воркер
        Worker worker = new Worker(name, q, this, logger);

        Thread t = threadFactory.newThread(worker);
        workers.add(t);
        t.start();
    }

    private BlockingQueue<Runnable> getQueueForWorker() {
        int index = rrIndex.getAndIncrement() % queues.size();
        return queues.get(index);
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            reject(command);
            return;
        }

        boolean offered = false;
        for (int i = 0; i < queues.size(); i++) {
            int index = rrIndex.getAndIncrement() % queues.size();
            BlockingQueue<Runnable> q = queues.get(index);

            if (q.offer(command)) {
                logger.info("[Pool] Task accepted into queue #" + index);
                offered = true;
                break;
            }
        }

        if (!offered) {
            if (currentThreads.get() < maxPoolSize) {
                startWorker();
                execute(command);
            } else {
                reject(command);
            }
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        CustomFuture<T> future = new CustomFuture<>();

        execute(() -> {
            try {
                T result = callable.call();
                future.complete(result);
            } catch (Throwable t) {
                future.fail(t);
            }
        });

        return future;
    }

    private void reject(Runnable command) {
        logger.info("[Rejected] Task " + command + " was rejected due to overload!");
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        logger.info("[Pool] Shutdown initiated");
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        for (BlockingQueue<Runnable> q : queues) {
            q.clear();
        }
        for (Thread t : workers) {
            t.interrupt();
        }
        logger.info("[Pool] Immediate shutdown");
    }

    public boolean isShutdown() { return isShutdown; }
    public long getKeepAliveTime() { return keepAliveTime; }
    public TimeUnit getTimeUnit() { return timeUnit; }
    public boolean canShrink() { return currentThreads.get() > corePoolSize; }

    public void onWorkerExit(Worker w) {
        currentThreads.decrementAndGet();
    }
}
