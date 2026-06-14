package com.example.cw_thread_pool;

import java.util.concurrent.*;

public interface CustomExecutor extends Executor {
    void execute(Runnable command);

    <T> Future<T> submit(Callable<T> callable);

    void shutdown();

    void shutdownNow();
}