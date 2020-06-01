package com.keray.common.support;

import com.alibaba.fastjson.JSON;
import com.keray.common.SpringContextHolder;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author keray
 * @date 2019/06/04 9:55
 */
@Component("redisLock")
public class RedisLock implements DistributedLock<String> {


    @Resource
    private RedisTemplate<String, String> template;


    @Override
    public String tryLock(String key, Consumer<String> callback) throws InterruptedException {
        try {
            return tryLock(key, callback, 0);
        } catch (TimeoutException ignore) {
        }
        return null;
    }

    @Override
    public String tryLock(String key, Consumer<String> callback, long timeout) throws InterruptedException, TimeoutException {
        String clockKey = "redis:clock:" + key;
        long now = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        int type = 0;
        while (true) {
            if (!(System.currentTimeMillis() - now < timeout || timeout == 0)) {
                type = 1;
                break;
            }
            if (currentThread.isInterrupted()) {
                type = 2;
                break;
            }
            String json = template.opsForValue().get(clockKey);
            if (json == null) {
                OLock lock = new OLock();
                lock.setHash(currentThread.hashCode());
                lock.setCount(1);
                Boolean result = template.opsForValue().setIfAbsent(clockKey, lock.toJson());
                template.expire(clockKey, 10, TimeUnit.MINUTES);
                if (result != null && result) {
                    // 加锁成功
                    execCallback(key, callback);
                    break;
                }
            } else {
                OLock lock = JSON.parseObject(json, OLock.class);
                if (lock.getHash() == currentThread.hashCode()) {
                    // 获取锁成功
                    lock.setCount(lock.getCount() + 1);
                    template.opsForValue().set(clockKey, lock.toJson());
                    template.expire(clockKey, timeout, TimeUnit.MILLISECONDS);
                    execCallback(key, callback);
                    break;
                }
            }
        }
        if (type == 2) {
            throw new InterruptedException("thread interrupted");
        }
        if (type == 1) {
            throw new TimeoutException("get lock " + key + "timeout : " + timeout);
        }
        return clockKey;
    }


    @Override
    public String tryLock(String key, long timeout) throws InterruptedException, TimeoutException {
        return tryLock(key, null, timeout);
    }

    @Override
    public String tryLock(String key) throws InterruptedException {
        try {
            return tryLock(key, 0);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void unLock(String key) {
        String clockKey = "redis::clock::" + key;
        OLock lock = JSON.parseObject(template.opsForValue().get(clockKey), OLock.class);
        if (lock == null) {
            return;
        }
        lock.setCount(lock.getCount() - 1);
        if (lock.getCount() == 0) {
            template.delete(clockKey);
        } else {
            template.opsForValue().set(clockKey, lock.toJson());
        }
    }

    private void execCallback(String key, Consumer<String> callback) {
        if (callback != null) {
            try {
                callback.accept(key);
            } finally {
                unLock(key);
            }
        }
    }


    private static DistributedLock<String> distributedLock;

    public static String lock(String key, Consumer<String> callback) throws TimeoutException, InterruptedException {
        if (distributedLock == null) {
            synchronized (RedisLock.class) {
                if (distributedLock == null) {
                    distributedLock = SpringContextHolder.getBean("redisLock");
                }
            }
        }
        return distributedLock.tryLock(key, callback, 60 * 1000);
    }

    public static String lock(String key) throws TimeoutException, InterruptedException {
        if (distributedLock == null) {
            synchronized (RedisLock.class) {
                if (distributedLock == null) {
                    distributedLock = SpringContextHolder.getBean(DistributedLock.class);
                }
            }
        }
        return distributedLock.tryLock(key, 60 * 1000);
    }

    public static void unlock(String key) {
        distributedLock.unLock(key);
    }
}

@Data
class OLock implements Serializable {
    int hash;
    int count;

    String toJson() {
        return JSON.toJSONString(this);
    }
}
