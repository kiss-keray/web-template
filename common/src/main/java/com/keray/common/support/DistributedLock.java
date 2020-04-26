package com.keray.common.support;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author keray
 * @date 2019/06/04 9:44
 * 分布式锁
 */
public interface DistributedLock<L extends Object> extends Serializable {
    L tryLock(String key, Consumer<String> callback) throws InterruptedException;

    L tryLock(String key, Consumer<String> callback, long timeout) throws InterruptedException, TimeoutException;

    L tryLock(String key, long timeout) throws TimeoutException, InterruptedException;

    L tryLock(String key) throws InterruptedException;

    void unLock(L lock);
}
