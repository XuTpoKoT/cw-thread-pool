package com.example.cw_thread_pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingThreadFactory implements ThreadFactory {

    private final String poolName;
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Logger logger;

    public LoggingThreadFactory(String poolName, Logger logger) {
        this.poolName = poolName;
        this.logger = logger;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = poolName + "-worker-" + counter.getAndIncrement();

        // Логирование только если оно включено
        logger.info("[ThreadFactory] Creating new thread: " + name);

        Thread t = new Thread(r, name);
        t.setDaemon(false);

        return t;
    }
}
