package com.example.cw_thread_pool;

import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {

        CustomThreadPool pool = new CustomThreadPool(
                2,
                4,
                5,
                5,
                TimeUnit.SECONDS,
                "MyPool",
                Logger.enabled()
        );

        for (int i = 0; i < 20; i++) {
            int id = i;

            pool.execute(() -> {
                System.out.println("[Task] start " + id);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task] end " + id);
            });
        }

        Future<Integer> f = pool.submit(() -> {
            Thread.sleep(500);
            return 118;
        });

        System.out.println("Future result: " + f.get());

        Thread.sleep(8000);

        pool.shutdown();
    }
}
