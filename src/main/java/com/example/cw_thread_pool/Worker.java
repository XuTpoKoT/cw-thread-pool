package com.example.cw_thread_pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final String name;
    private final BlockingQueue<Runnable> queue;
    private final CustomThreadPool pool;
    private final Logger logger; // <-- Добавляем логгер

    public Worker(String name, BlockingQueue<Runnable> queue, CustomThreadPool pool, Logger logger) {
        this.name = name;
        this.queue = queue;
        this.pool = pool;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (pool.isShutdown() && queue.isEmpty()) {
                    logger.info("[Worker] " + name + " terminated.");
                    return;
                }

                // poll вернет null, если время вышло
                Runnable task = queue.poll(pool.getKeepAliveTime(), pool.getTimeUnit());

                if (task == null) {
                    if (pool.canShrink()) {
                        logger.info("[Worker] " + name + " idle timeout, stopping.");
                        pool.onWorkerExit(this);
                        return;
                    }
                    continue;
                }

                logger.info("[Worker] " + name + " executes task");

                try {
                    task.run();
                } catch (Exception e) {
                    // Здесь можно использовать logger.warn, если нужно
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("[Worker] " + name + " interrupted.");
        }
    }
}
