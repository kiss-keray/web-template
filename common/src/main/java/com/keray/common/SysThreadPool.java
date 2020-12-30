package com.keray.common;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author by keray
 * date:2019/9/16 11:49
 */
public class SysThreadPool {
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(500, 1000, 10,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100000),
            r -> {
                Thread t = new Thread(r);
                t.setName("sys-thread-" + COUNT.getAndIncrement());
                return t;
            });

    public static void execute(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }


    public static Future<?> submit(Runnable runnable) {
        return threadPoolExecutor.submit(runnable);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return threadPoolExecutor.submit(task);
    }

    public static void close() {
        threadPoolExecutor.shutdownNow();
    }
}
