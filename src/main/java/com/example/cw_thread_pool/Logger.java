package com.example.cw_thread_pool;

public interface Logger {
    void info(String message);

    static Logger enabled() {
        return System.out::println;
    }

    static Logger disabled() {
        return (msg) -> {};
    }
}
