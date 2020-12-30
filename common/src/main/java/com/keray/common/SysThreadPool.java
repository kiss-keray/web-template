package com.keray.common;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private volatile static IUserContext userContext = null;

    public static void execute(Runnable runnable) {
        execute(runnable, true);
    }

    public static void execute(Runnable runnable, boolean useContext) {
        if (useContext) {
            if (userContext == null) {
                userContext = SpringContextHolder.getBean(IUserContext.class);
            }
            Map<String, Object> context = userContext.export();
            threadPoolExecutor.execute(() -> {
                userContext.importConf(context);
                runnable.run();
                userContext.clear();
            });
        } else {
            threadPoolExecutor.execute(runnable);
        }
    }

    public static Future<?> submit(Runnable runnable) {
        if (userContext == null) {
            userContext = SpringContextHolder.getBean(IUserContext.class);
        }
        Map<String, Object> context = userContext.export();
        return threadPoolExecutor.submit(() -> {
            userContext.importConf(context);
            runnable.run();
            userContext.clear();
        });
    }

    public static void close() {
        threadPoolExecutor.shutdownNow();
    }
}
