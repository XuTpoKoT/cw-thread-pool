package com.example.cw_thread_pool;

import java.util.concurrent.*;

public class CustomFuture<T> implements Future<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private T result;
    private Throwable error;
    private volatile boolean cancelled = false;

    void complete(T result) {
        this.result = result;
        latch.countDown();
    }

    void fail(Throwable t) {
        this.error = t;
        latch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled = true;
        latch.countDown();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        if (error != null) throw new ExecutionException(error);
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!latch.await(timeout, unit)) throw new TimeoutException();
        if (error != null) throw new ExecutionException(error);
        return result;
    }
}
