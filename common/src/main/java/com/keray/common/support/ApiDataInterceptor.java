package com.keray.common.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.keray.common.SysThreadPool;
import com.keray.common.annotation.ApiRecord;
import com.keray.common.config.ApiDataConfig;
import com.keray.common.utils.CommonUtil;
import com.keray.common.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/10/21 14:52
 */
@Component("apiDataInterceptor")
@ConditionalOnProperty(value = "keray.api.data", havingValue = "true")
@Slf4j
public class ApiDataInterceptor extends HandlerInterceptorAdapter {

    @Resource(name = "apiRedisRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ApiDataConfig apiDataConfig;

    private final Object secondClock = new Object();
    private final Object minuteClock = new Object();
    private final Object hourClock = new Object();
    private final Object dayClock = new Object();

    private final int secondSyncScope = RandomUtil.randomInt(5);


    private ConcurrentHashMap<String, ApiTimeData> apiTimeDataLinkedHashMap = new ConcurrentHashMap<>(128);

    private volatile ApiTimeData minuteData = new ApiTimeData("", null);
    private volatile ApiTimeData hourData = new ApiTimeData("", null);
    private volatile ApiTimeData dayData = new ApiTimeData("", null);
    private volatile String hourSyncKey = "";
    private volatile String daySyncKey = "";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            if (isApiRecord((HandlerMethod) handler)) {
                LocalDateTime now = LocalDateTime.now();
                SysThreadPool.execute(() -> {
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    String apiKey = handlerMethod.getMethod().getDeclaringClass().getName() + "#" + handlerMethod.getMethod().getName();
                    SysThreadPool.execute(() -> {
                        try {
                            addSecondData(now, apiKey);
                        } catch (TimeoutException | InterruptedException ignored) {
                        }
                    });
                    SysThreadPool.execute(() -> {
                        try {
                            addMinuteData(now, apiKey);
                        } catch (TimeoutException | InterruptedException ignored) {
                        }
                    });
                    SysThreadPool.execute(() -> {
                        try {
                            addHourData(now, apiKey);
                        } catch (TimeoutException | InterruptedException ignored) {
                        }
                    });
                    SysThreadPool.execute(() -> {
                        try {
                            addDayData(now, apiKey);
                        } catch (TimeoutException | InterruptedException ignored) {
                        }
                    });
                });
            }
        }
        return true;
    }


    public static Map<String, Integer> loadOldData(Set<String> setData) {
        if (setData == null) {
            return new HashMap<>(4);
        }
        return setData.stream()
                .parallel()
                .map(JSON::parseObject)
                .filter(MapUtil::isNotEmpty)
                .collect(Collectors.toMap(
                        jsonObject -> {
                            for (String setKey : jsonObject.keySet()) {
                                return setKey;
                            }
                            return null;
                        },
                        jsonObject -> {
                            for (String setKey : jsonObject.keySet()) {
                                return jsonObject.getInteger(setKey);
                            }
                            return 0;
                        },
                        Integer::sum
                ));
    }

    @PreDestroy
    public void close() {
        log.info("关闭api数据同步");
        try {
            if (apiTimeDataLinkedHashMap.size() > 0) {
                log.info("秒级级数据同步:{}", apiTimeDataLinkedHashMap);
                syncData(apiDataConfig.getApiDataSecondKey(), new LinkedHashMap<>(apiTimeDataLinkedHashMap),
                        k -> redisTemplate.expire(k, apiDataConfig.getSecondScope(), TimeUnit.SECONDS));
            }
            if (minuteData.getApiData() != null) {
                final LinkedHashMap<String, ApiTimeData> minuteCopyData = new LinkedHashMap<>();
                minuteCopyData.put(minuteData.timeKey, minuteData);
                log.info("分钟级数据同步:{}", minuteCopyData);
                syncData(apiDataConfig.getApiDataMinuteKey(), minuteCopyData,
                        k -> redisTemplate.expire(k, apiDataConfig.getMinuteScope(), TimeUnit.MINUTES));
            }
            if (hourData.getApiData() != null) {
                final LinkedHashMap<String, ApiTimeData> hourCopyData = new LinkedHashMap<>();
                hourCopyData.put(hourData.timeKey, hourData);
                log.info("小时级数据同步:{}", hourCopyData);
                syncData(apiDataConfig.getApiDataHourKey(), hourCopyData,
                        k -> redisTemplate.expire(k, apiDataConfig.getHourScope(), TimeUnit.HOURS));
            }
            if (dayData.getApiData() != null) {
                final LinkedHashMap<String, ApiTimeData> dayCopyData = new LinkedHashMap<>();
                dayCopyData.put(dayData.timeKey, dayData);
                log.info("天级数据同步:{}", dayCopyData);
                syncData(apiDataConfig.getApiDataDayKey(), dayCopyData,
                        k -> redisTemplate.expire(k, apiDataConfig.getDayScope(), TimeUnit.DAYS));
            }

        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void addSecondData(LocalDateTime now, String key) throws TimeoutException, InterruptedException {
        String nowKey = TimeUtil.DATE_TIME_FORMATTER_SC.format(now);
        Consumer<String> add = timeKey -> {
            ApiTimeData nowTimeData = apiTimeDataLinkedHashMap.get(timeKey);
            // 自增一次
            if (nowTimeData.getApiData().containsKey(key)) {
                nowTimeData.getApiData().get(key).getAndIncrement();
            } else {
                AtomicInteger res = nowTimeData.getApiData().putIfAbsent(key, new AtomicInteger(1));
                if (res != null) {
                    res.getAndIncrement();
                }
            }
        };
        // 原先存在key
        if (apiTimeDataLinkedHashMap.containsKey(nowKey)) {
            add.accept(nowKey);
        } else {
            // 保存当前数据
            ConcurrentHashMap<String, AtomicInteger> apiData = initMap(key);
            apiData.get(key).getAndIncrement();
            // 开始分段同步
            all:
            if (apiTimeDataLinkedHashMap.size() >= secondSyncScope) {
                LinkedHashMap<String, ApiTimeData> copyData = null;
                synchronized (secondClock) {
                    if (apiTimeDataLinkedHashMap.containsKey(nowKey)) {
                        break all;
                    }
                    copyData = new LinkedHashMap<>(apiTimeDataLinkedHashMap);
                    apiTimeDataLinkedHashMap.clear();
                    apiTimeDataLinkedHashMap.put(nowKey, new ApiTimeData(nowKey, apiData));
                }
                // 同步数据
                syncData(apiDataConfig.getApiDataSecondKey(), copyData,
                        k -> redisTemplate.expire(k, apiDataConfig.getSecondScope(), TimeUnit.SECONDS));
                return;
            }
            ApiTimeData res = apiTimeDataLinkedHashMap.putIfAbsent(nowKey, new ApiTimeData(nowKey, apiData));
            if (res != null) {
                add.accept(nowKey);
            }
        }
    }


    private void addMinuteData(LocalDateTime now, String key) throws TimeoutException, InterruptedException {
        String timeKey = TimeUtil.DATE_TIME_FORMATTER_MINUTE.format(now);
        all:
        if (!minuteData.timeKey.equals(timeKey)) {
            // 保存当前数据
            ApiTimeData oldData;
            synchronized (minuteClock) {
                if (minuteData.timeKey.equals(timeKey)) {
                    break all;
                }
                oldData = minuteData;
                minuteData = new ApiTimeData(timeKey, initMap(key));
            }
            if (oldData.getApiData() != null) {
                // 同步数据
                final LinkedHashMap<String, ApiTimeData> copyData = new LinkedHashMap<>();
                copyData.put(oldData.timeKey, oldData);
                syncData(apiDataConfig.getApiDataMinuteKey(), copyData,
                        k -> redisTemplate.expire(k, apiDataConfig.getMinuteScope(), TimeUnit.MINUTES));
            }
        }
        if (minuteData.apiData.containsKey(key)) {
            minuteData.apiData.get(key).getAndIncrement();
        } else {
            AtomicInteger res = minuteData.apiData.putIfAbsent(key, new AtomicInteger(1));
            if (res != null) {
                res.getAndIncrement();
            }
        }
    }


    private void addHourData(LocalDateTime now, String key) throws TimeoutException, InterruptedException {
        String timeKey = TimeUtil.DATE_TIME_FORMATTER_HOUR.format(now);
        Consumer<ApiTimeData> sync = (oldData) -> {
            if (oldData.getApiData() != null) {
                try {
                    // 同步数据
                    final LinkedHashMap<String, ApiTimeData> copyData = new LinkedHashMap<>();
                    copyData.put(oldData.timeKey, oldData);
                    syncData(apiDataConfig.getApiDataHourKey(), copyData,
                            k -> redisTemplate.expire(k, apiDataConfig.getHourScope(), TimeUnit.HOURS));
                } catch (TimeoutException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        all:
        if (!hourData.timeKey.equals(timeKey)) {
            // 保存当前数据
            ApiTimeData oldData;
            synchronized (hourClock) {
                if (hourData.timeKey.equals(timeKey)) {
                    break all;
                }
                oldData = hourData;
                hourData = new ApiTimeData(timeKey, initMap(key));
            }
            sync.accept(oldData);
        } else {
            if (now.getMinute() % apiDataConfig.getHourSyncMinute() == 0 && now.getMinute() != 0) {
                String syncKey = TimeUtil.DATE_TIME_FORMATTER_MINUTE.format(now);
                synchronized (hourClock) {
                    if (!hourSyncKey.equals(syncKey)) {
                        sync.accept(hourData);
                        hourSyncKey = syncKey;
                    }
                }
            }
        }
        if (hourData.apiData.containsKey(key)) {
            hourData.apiData.get(key).getAndIncrement();
        } else {
            AtomicInteger res = hourData.apiData.putIfAbsent(key, new AtomicInteger(1));
            if (res != null) {
                res.getAndIncrement();
            }
        }
    }

    private void addDayData(LocalDateTime now, String key) throws TimeoutException, InterruptedException {
        String timeKey = TimeUtil.DATE_TIME_FORMATTER_DAY.format(now);
        Consumer<ApiTimeData> sync = (oldData) -> {
            if (oldData.getApiData() != null) {
                try {
                    // 同步数据
                    final LinkedHashMap<String, ApiTimeData> copyData = new LinkedHashMap<>();
                    copyData.put(oldData.timeKey, oldData);
                    syncData(apiDataConfig.getApiDataDayKey(), copyData,
                            k -> redisTemplate.expire(k, apiDataConfig.getDayScope(), TimeUnit.DAYS));
                } catch (TimeoutException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        all:
        if (!dayData.timeKey.equals(timeKey)) {
            // 保存当前数据
            ApiTimeData oldData;
            synchronized (dayClock) {
                if (dayData.timeKey.equals(timeKey)) {
                    break all;
                }
                oldData = dayData;
                dayData = new ApiTimeData(timeKey, initMap(key));
            }
            sync.accept(oldData);
        } else {
            if (now.getMinute() % apiDataConfig.getDaySyncMinute() == 0 && now.getMinute() != 0) {
                String syncKey = TimeUtil.DATE_TIME_FORMATTER_MINUTE.format(now);
                synchronized (dayClock) {
                    if (!daySyncKey.equals(syncKey)) {
                        sync.accept(dayData);
                        daySyncKey = syncKey;
                    }
                }
            }
        }
        if (dayData.apiData.containsKey(key)) {
            dayData.apiData.get(key).getAndIncrement();
        } else {
            AtomicInteger res = dayData.apiData.putIfAbsent(key, new AtomicInteger(1));
            if (res != null) {
                res.getAndIncrement();
            }
        }
    }

    private ConcurrentHashMap<HandlerMethod, Boolean> apiRecordCache = new ConcurrentHashMap<>(256);

    private boolean isApiRecord(HandlerMethod method) {
        Boolean cacheRes = apiRecordCache.get(method);
        if (cacheRes != null) {
            return cacheRes;
        }
        boolean result;
        if (method.getMethodAnnotation(ApiRecord.class) != null) {
            result = true;
        } else {
            result = CommonUtil.classAllSuperHaveAnnotation(method.getMethod().getDeclaringClass(), ApiRecord.class);
        }
        apiRecordCache.putIfAbsent(method, result);
        return result;
    }

    private void syncData(String keyPre, LinkedHashMap<String, ApiTimeData> finalData, Consumer<String> redisKeyExpire) throws TimeoutException, InterruptedException {
        log.debug("同步api记录{}", finalData);
        RedissonLock.lock(keyPre, (k) -> finalData.keySet().stream()
                .parallel()
                .forEach(s -> {
                    ApiTimeData data = finalData.get(s);
                    String redisKey = keyPre + data.getRedisKey();
                    BoundSetOperations<String, String> redisData = redisTemplate.boundSetOps(redisKey);
                    // 获取原先的数据
                    Map<String, Integer> oldData = loadOldData(redisData.members());
                    // 寻找原数据与现数据的并集
                    List<String> deleteValue = data.getApiData().keySet()
                            .stream()
                            .filter(oldData::containsKey)
                            .peek(k1 -> data.getApiData().get(k1).addAndGet(oldData.get(k1)))
                            .map(k1 -> StrUtil.format("{\"{}\":{}}", k1, oldData.get(k1)))
                            .collect(Collectors.toCollection(LinkedList::new));
                    // 移除并集
                    if (CollUtil.isNotEmpty(deleteValue)) {
                        redisTemplate.opsForSet().remove(redisKey, deleteValue.toArray());
                    }
                    Boolean expire = redisTemplate.hasKey(redisKey);

                    // 添加记录数据
                    redisTemplate.opsForSet().add(redisKey, data.getApiData().keySet()
                            .stream()
                            .map(k1 -> {
                                String result = StrUtil.format("{\"{}\":{}}", k1, data.apiData.get(k1).get());
                                // 同步后将本机记录清零 在秒级，分钟级同步时本记录肯定已经为不可变
                                // 小时级和天级时非不可变同步时使用了锁 也保证了唯一同步
                                data.apiData.get(k1).set(0);
                                return result;
                            })
                            .toArray(String[]::new));
                    if (Boolean.FALSE.equals(expire)) {
                        redisKeyExpire.accept(redisKey);
                    }
                }));
    }


    private ConcurrentHashMap<String, AtomicInteger> initMap(String key) {
        ConcurrentHashMap<String, AtomicInteger> apiData = new ConcurrentHashMap<>(128);
        apiData.put(key, new AtomicInteger(0));
        return apiData;
    }

    @AllArgsConstructor
    @Data
    public static class ApiTimeData {
        private String timeKey;
        private ConcurrentHashMap<String, AtomicInteger> apiData;

        public String getRedisKey() {
            return timeKey.replaceAll(" ", "#");
        }
    }

    @AllArgsConstructor
    @Data
    public static class ApiTimeDataVo {
        private String timeKey;
        private Map<String, Integer> apiData;

    }
}
