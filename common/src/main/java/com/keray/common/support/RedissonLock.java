package com.keray.common.support;

import com.keray.common.SpringContextHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author by keray
 * date:2020/1/10 10:00 AM
 */
@Component("redissonLock")
@ConditionalOnBean(RedissonClient.class)
public class RedissonLock implements DistributedLock<RLock> {
    @Resource
    private RedissonClient redissonClient;


    @Override
    public RLock tryLock(String key, Consumer<String> callback) throws InterruptedException {
        try {
            return tryLock(key, callback, 60_000);
        } catch (TimeoutException ignore) {
        }
        return null;
    }

    @Override
    public RLock tryLock(String key, Consumer<String> callback, long timeout) throws InterruptedException, TimeoutException {
        String clockKey = "redis:clock:" + key;
        RLock rLock = redissonClient.getLock(clockKey);
        rLock.tryLock(timeout, TimeUnit.MILLISECONDS);
        execCallback(key, callback, rLock);
        return rLock;
    }


    @Override
    public RLock tryLock(String key, long timeout) throws TimeoutException, InterruptedException {
        return tryLock(key, null, timeout);
    }

    @Override
    public RLock tryLock(String key) throws InterruptedException {
        try {
            tryLock(key, 0);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void unLock(RLock lock) {
        if (lock != null) {
            lock.unlock();
        }
    }

    private void execCallback(String key, Consumer<String> callback, RLock rLock) {
        if (callback != null) {
            try {
                callback.accept(key);
            } finally {
                unLock(rLock);
            }
        }
    }


    private static DistributedLock<RLock> distributedLock;

    public static RLock lock(String key, Consumer<String> callback) throws TimeoutException, InterruptedException {
        if (distributedLock == null) {
            synchronized (RedisLock.class) {
                if (distributedLock == null) {
                    distributedLock = SpringContextHolder.getBean("redissonLock");
                }
            }
        }
        return distributedLock.tryLock(key, callback, 60_000);
    }

    public static RLock lock(String key) throws TimeoutException, InterruptedException {
        if (distributedLock == null) {
            synchronized (RedisLock.class) {
                if (distributedLock == null) {
                    distributedLock = SpringContextHolder.getBean(DistributedLock.class);
                }
            }
        }
        return distributedLock.tryLock(key, 60_000);
    }

    public static void unlock(RLock lock) {
        distributedLock.unLock(lock);
    }
}
