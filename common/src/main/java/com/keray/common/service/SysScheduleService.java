package com.keray.common.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keray.common.*;
import com.keray.common.annotation.KSchedule;
import com.keray.common.annotation.KScheduleDelay;
import com.keray.common.service.ienum.ScheduleStatus;
import com.keray.common.service.mapper.SysScheduleMapper;
import com.keray.common.service.model.SysScheduleModel;
import com.keray.common.support.RedissonLock;
import com.keray.common.utils.KZEngine;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author by keray
 * date:2019/9/5 16:43
 */
@Aspect
@Service(value = "sysScheduleService")
@Slf4j
@ConditionalOnProperty(value = "keray.schedule", havingValue = "true")
public class SysScheduleService implements BaseService<SysScheduleModel> {
    private final ThreadLocal<Boolean> scheduleExecFlag = new ThreadLocal<>();
    private final String driverId = RandomUtil.randomString(64);
    private final ScheduledExecutorService schedulingException = new ScheduledThreadPoolExecutor(10, r -> {
        Thread t = new Thread(r);
        t.setName("kSchedule-pool");
        return t;
    });
    @Resource
    private SysScheduleMapper sysScheduleMapper;

    @Override
    public IBaseMapper<SysScheduleModel> getMapper() {
        return sysScheduleMapper;
    }

    public SysScheduleModel updateScheduleStatus(String id, ScheduleStatus status) {
        log.info("sysSchedule 状态更新:id={} -> {}", id, status);
        SysScheduleModel model = new SysScheduleModel();
        LocalDateTime now = LocalDateTime.now();
        model.setId(id);
        model.setStatus(status);
        model.setModifyTime(now);
        if (ScheduleStatus.exec == status) {
            model.setExecTime(now);
        }
        sysScheduleMapper.updateById(model);
        model.setExecTime(null);
        return model;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 18:24</h3>
     * 项目启动任务初始化
     * </p>
     *
     * @param
     * @return <p> {@link } </p>
     * @throws
     */
    @EventListener
    public void scheduleInit(ApplicationStartedEvent startedEvent) {
        SysThreadPool.execute(() -> {
            List<SysScheduleModel> sysScheduleModels = selectList(
                    Wrappers.lambdaQuery(new SysScheduleModel())
                            .ne(SysScheduleModel::getStatus, ScheduleStatus.success)
                            .ne(SysScheduleModel::getStatus, ScheduleStatus.fail)
                            // 不取执行中的任务，exec任务保证有机器正在执行
                            .ne(SysScheduleModel::getStatus, ScheduleStatus.exec)
            );
            sysScheduleModels.stream()
                    .parallel()
                    .forEach(m -> dataScheduleSubmit(m, false));
        }, false);
    }


    @PreDestroy
    public void close() {
        log.info("关闭任务执行器");
        schedulingException.shutdownNow();
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/6 14:05</h3>
     * 每天凌晨2点扫描任务
     * 凌晨扫描的任务只获取等待提交的任务
     * </p>
     *
     * @param
     * @return <p> {@link} </p>
     * @throws
     */
    @Scheduled(cron = "0 0 2 * * ? ")
    public void dayScanSchedule() {
        List<SysScheduleModel> sysScheduleModels = selectList(
                Wrappers.lambdaQuery(new SysScheduleModel())
                        .eq(SysScheduleModel::getStatus, ScheduleStatus.waitSubmit)
        );
        log.info("扫描的等待提交的任务：tasks={}", JSON.toJSON(sysScheduleModels));
        sysScheduleModels.stream()
                .parallel()
                .forEach(m -> dataScheduleSubmit(m, false));
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/6 10:41</h3>
     * 提交SysScheduleModel对象任务
     * </p>
     *
     * @param model
     * @return <p> {@link } </p>
     * @throws
     */
    public boolean dataScheduleSubmit(SysScheduleModel model, boolean nowExec) {
        SysScheduleModel newModel = SysScheduleModel.builder()
                .driverId(driverId)
                .status(ScheduleStatus.waitSubmit)
                .build();
        if (sysScheduleMapper.update(newModel,
                Wrappers.lambdaUpdate(new SysScheduleModel())
                        .eq(BaseEntity::getId, model.getId())
                        .eq(SysScheduleModel::getDriverId, model.getDriverId())
        ) == 1) {
            try {
                // 更新成功 提交任务
                log.info("开始发序列化任务：{}", model);
                JSONObject kzCron = JSON.parseObject(model.getKzCron());
                JSONObject methodDetail = JSON.parseObject(model.getMethodDetail());
                String methodName = methodDetail.getString("name");
                Class[] sign = methodDetail.getJSONArray("sign") == null ? null :
                        methodDetail.getJSONArray("sign").stream().map(className -> {
                            try {
                                return Class.forName(String.valueOf(className));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }).toArray(Class[]::new);
                JSONArray args = methodDetail.getJSONArray("args");
                Object[] argsValue = null;
                if (CollUtil.isNotEmpty(args)) {
                    argsValue = args.stream().map(v -> {
                        Map<String, String> detail = (Map<String, String>) v;
                        try {
                            if (detail.get("value") == null) {
                                return null;
                            }
                            // 先基本类型转换
                            try {
                                return Convert.convert(Class.forName(detail.get("clazz")), detail.get("value"));
                            } catch (Exception e) {
                                log.warn("value转换基本类型失败:value={},clazz={}", detail.get("value"), detail.get("value").getClass());
                            }
                            return JSON.parseObject(detail.get("value"), Class.forName(detail.get("clazz")));
                        } catch (ClassNotFoundException e) {
                            log.error("任务方法反序列失败：", e);
                            throw new RuntimeException(e);
                        }
                    }).toArray();
                }
                log.info("参数反序列化完成:{}", argsValue == null ? null : JSON.toJSON(argsValue));
                Object bean = SpringContextHolder.getBean(model.getBeanName());
                Method method = null;
                // 如果是spring aop代理类 拿到真实对象的method
                if (bean instanceof SpringProxy) {
                    method = bean.getClass().getSuperclass().getMethod(methodName, sign);
                } else {
                    method = bean.getClass().getMethod(methodName, sign);
                }
                if (nowExec) {
                    try {
                        scheduleExecFlag.set(true);
                        if (exec(model.getId(), model.getBeanName(), method, argsValue, updateScheduleStatus(model.getId(), ScheduleStatus.waitExec))) {
                            updateScheduleStatus(model.getId(), ScheduleStatus.success);
                            return true;
                        } else {
                            // 立即执行模式中不重试执行
                            log.error("任务执行返回false");
                            updateScheduleStatus(model.getId(), ScheduleStatus.fail);
                            return false;
                        }
                    } finally {
                        scheduleExecFlag.remove();
                    }
                }
                submit(model.getId(), model.getCreateTime(), 0, driverId,
                        kzCron.getString("kz"), kzCron.getString("cron"), model.getBeanName(), model.getRetryCount(), model.getRetryMillis(),
                        method, argsValue
                );
            } catch (Exception e) {
                log.error("任务反序列失败:", e);
                // 立即执行的任务抛出异常保证事务有效
                if (nowExec) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 失败不做任何事，只有在分布式下被其他节点抢先提交了才会失败
        return false;
    }


    @Pointcut("@annotation(com.keray.common.annotation.KSchedule)")
    public void setSysSchedule() {
    }

    @Around("setSysSchedule()")
    public Object setSysSchedule(ProceedingJoinPoint pjp) {
        try {
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) pjp;
            Field proxy = methodPoint.getClass().getDeclaredField("methodInvocation");
            proxy.setAccessible(true);
            ReflectiveMethodInvocation j = (ReflectiveMethodInvocation) proxy.get(methodPoint);
            Method method = j.getMethod();
            KSchedule kSchedule = method.getAnnotation(KSchedule.class);
            // 检查任务是否可直接执行 可执行直接返回
            if (scheduleExecFlag.get() != null && scheduleExecFlag.get()) {
                return pjp.proceed();
            }
            if (saveSchedule(kSchedule, method, pjp.getArgs())) {
                return pjp.proceed();
            }
        } catch (Throwable e) {
            log.error("任务提交失败", e);
        }
        return null;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/11/5 15:50</h3>
     * 立即执行任务
     * </p>
     *
     * @param scheduleId
     * @return <p> {@link boolean} </p>
     * @throws
     */
    public boolean nowExec(String scheduleId) {
        SysScheduleModel scheduleModel = getById(scheduleId);
        return this.dataScheduleSubmit(scheduleModel, true);
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/11/5 16:05</h3>
     * 取消任务
     * </p>
     *
     * @param scheduleId
     * @return <p> {@link boolean} </p>
     * @throws
     */
    public boolean cancel(String scheduleId) {
        this.updateScheduleStatus(scheduleId, ScheduleStatus.cancel);
        return true;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 21:45</h3>
     * 持久化任务
     * </p>
     *
     * @param kSchedule
     * @param method
     * @param args
     * @return <p> {@link boolean} </p>
     * @throws
     */
    private boolean saveSchedule(KSchedule kSchedule, Method method, Object[] args) {
        LocalDateTime submitNow = LocalDateTime.now();
        // 没有kz和cron表达式的任务直接直接执行 并且关闭了动态延迟
        if (StrUtil.isBlank(kSchedule.kz()) && StrUtil.isBlank(kSchedule.cron()) && !kSchedule.dynamicDelay()) {
            return true;
        }
        // 校验参数是否全部实现Serialization接口
        if (args != null) {
            for (Object obj : args) {
                if (obj != null && !(obj instanceof Serializable)) {
                    throw new IllegalStateException("提交任务的方法参数必须全部实现Serializable接口");
                }
            }
        }
        // 校验kz
        if (StrUtil.isNotBlank(kSchedule.kz()) && !KZEngine.checkKZ(kSchedule.kz())) {
            throw new IllegalStateException("kz表达式错误：kz" + kSchedule.kz());
        }
        // 校验cron

        // 设备id
        String driverId = this.driverId;
        // 保存任务到数据库
        SysScheduleModel sysScheduleModel = SysScheduleModel.builder()
                .beanName(kSchedule.beanName())
                .retryCount(kSchedule.maxRetry())
                .status(ScheduleStatus.waitSubmit)
                .retryMillis(kSchedule.retryMillis())
                .driverId(driverId)
                .scheduleDesc(kSchedule.desc())
                .platExecTime(submitNow.minus(-computeDelay(submitNow, kSchedule.kz(), method, args), ChronoUnit.MILLIS))
                .kzCron(JSON.toJSONString(
                        MapUtil.builder()
                                .put("kz", kSchedule.kz())
                                .put("cron", kSchedule.cron())
                                .build()
                ))
                .methodDetail(JSON.toJSONString(
                        MapUtil.builder()
                                .put("name", method.getName())
                                .put("sign", Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList()))
                                .put("args", args == null ? null : Stream.of(args)
                                        .map(value -> MapUtil.builder()
                                                .put("clazz", value == null ? null : value.getClass().getName())
                                                .put("value", JSON.toJSON(value))
                                                .build())
                                        .collect(Collectors.toList()))
                                .build()
                ))
                .build();
        insert(sysScheduleModel);
        // 提交任务
        submit(sysScheduleModel.getId(), submitNow, 0, driverId,
                kSchedule.kz(), kSchedule.cron(), kSchedule.beanName(), kSchedule.maxRetry(), kSchedule.retryMillis(),
                method, args
        );
        return false;
    }


    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 16:54</h3>
     * 提交任务
     * </p>
     *
     * @param id        任务id
     * @param startTime 任务提交时间
     * @param kz
     * @param cron
     * @param beanName
     * @param method    执行方法
     * @param args      方法参数
     */
    private void submit(String id, LocalDateTime startTime, int retryCount, String driverId,
                        String kz, String cron, String beanName, int retryMaxCount, int retryMS,
                        Method method, Object[] args) {
        log.info("提交任务:id={},startTime={},retryCount={},driverId={},kz={},cron={},beanName={},retryMaxCount={},retryMS={},method={},args={}",
                id, startTime, retryCount, driverId, kz, cron, beanName, retryMaxCount, retryMS, method, args
        );
        if (StrUtil.isNotBlank(cron) && StrUtil.isBlank(kz)) {
            log.error("暂时不支持cron方式");
            throw new RuntimeException("no support");
        }
        if (retryCount > retryMaxCount) {
            log.warn("任务重试次数达到最大值，直接失败任务");
            updateScheduleStatus(id, ScheduleStatus.fail);
            return;
        }
        long delay = computeDelay(startTime, kz, method, args);
        if (delay < 0) {
            log.warn("定义执行时间已超时，立即执行任务");
            delay = 0;
        }
        // 如果任务执行时间在一天后退出
        if (delay > 24 * 60 * 60 * 1000) {
            log.info("任务延迟超过1天，暂时不提交任务，等待下次轮询提交 delay={}", delay);
            return;
        }
        final long finalDelay = delay;
        // 使用线程提交 保证不被外部事物影响执行
        SysThreadPool.execute(() -> {
            // 保存任务提交为wait_exec的定格数据，用于执行前的定格数据检查
            SysScheduleModel model = updateScheduleStatus(id, ScheduleStatus.waitExec);
            log.info("提交执行任务:id={},delay={}", id, finalDelay);
            // 任务runnable
            Runnable run = () -> {
                try {
                    String owner = this.driverId;
                    if (!driverId.equals(owner)) {
                        log.warn("任务设定执行设备不是本设备，放弃执行 driverId={},owner={}", driverId, owner);
                        return;
                    }
                    scheduleExecFlag.set(true);
                    if (exec(id, beanName, method, args, model)) {
                        updateScheduleStatus(id, ScheduleStatus.success);
                    } else {
                        log.warn("任务执行异常返回false:重试执行");
                        if (retryMaxCount > 0) {
                            updateScheduleStatus(id, ScheduleStatus.waitRetry);
                            submit(id, startTime.minus(-retryMS, ChronoUnit.MILLIS), retryMaxCount, driverId, kz, cron, beanName, retryCount + 1, retryMS, method, args);
                        } else {
                            updateScheduleStatus(id, ScheduleStatus.fail);
                        }
                    }
                } catch (CheckException ignore) {
                } catch (Exception e) {
                    log.error("任务执行异常", e);
                    updateScheduleStatus(id, ScheduleStatus.fail);
                } finally {
                    scheduleExecFlag.remove();
                }
            };
            // 动态时间

            // kz表达式任务提 & cron
            if (StrUtil.isNotBlank(kz) && StrUtil.isNotBlank(cron)) {
                // 提交kz 占时忽略cron
                schedulingException.schedule(run, finalDelay, TimeUnit.MILLISECONDS);
            }
            // cron 表达式任务提交到spring schedule
            else if (StrUtil.isNotBlank(cron)) {
                // 在KSchedule设定的延迟时间执行cron
                log.error("暂时不支持cron方式");
                throw new RuntimeException("no support");
            }
            // 提交纯延迟任务
            else {
                schedulingException.schedule(run, finalDelay, TimeUnit.MILLISECONDS);
            }
        });
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/6 15:27</h3>
     * 计算任务延迟时间
     * </p>
     *
     * @param startTime 创建开始时间
     * @param kz
     * @param method
     * @param args
     * @return <p> {@link long} </p>
     * @throws
     */
    private long computeDelay(LocalDateTime startTime, String kz, Method method, Object[] args) {
        // 传递的动态延迟 优先级高于kz
        Annotation[][] annotations = method.getParameterAnnotations();
        all:
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i] != null && annotations[i].length > 0) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation.annotationType() == KScheduleDelay.class) {
                        Object o = args[i];
                        if (o == null) {
                            log.warn("schedule执行传入的delay为null");
                        } else if (o instanceof Integer || o instanceof Long) {
                            return Long.parseLong(o.toString());
                        } else if (o instanceof String) {
                            kz = (String) o;
                            break all;
                        } else {
                            log.warn("schedule执行传入的delay类型错误或者为null，仅支持 Integer,Long,int,long,String");
                        }
                        break all;
                    }
                }
            }
        }
        // 校验kz
        if (!KZEngine.checkKZ(kz)) {
            throw new IllegalStateException("kz表达式错误：kz" + kz);
        }

        // kz计算延迟
        if (StrUtil.isNotBlank(kz)) {
            return LocalDateTime.now().until(KZEngine.computeTime(kz, startTime), ChronoUnit.MILLIS);
        }
        return 0;
    }


    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 18:04</h3>
     * </p>
     *
     * @param id       任务id
     * @param beanName
     * @param method
     * @param args
     * @return <p> {@link } </p>
     * @throws
     */
    private boolean exec(String id, String beanName, Method method, Object[] args, SysScheduleModel oldModel) throws InvocationTargetException, IllegalAccessException, TimeoutException, InterruptedException {
        RLock lock = RedissonLock.lock("k-schedule" + id);
        try {
            SysScheduleModel old = sysScheduleMapper.selectById(id);
            if (old.getStatus() != ScheduleStatus.waitExec || old.getDriverId().equals(oldModel.getDriverId())) {
                throw new CheckException("任务被执行");
            }
            updateScheduleStatus(id, ScheduleStatus.exec);
            try {
                Object execObj = SpringContextHolder.getBean(beanName);
                Object result = method.invoke(execObj, args);
                if (result instanceof Boolean) {
                    return (boolean) result;
                }
                return true;
            } catch (Exception e) {
                log.error("任务执行失败:" + id, e);
                throw new RuntimeException("任务执行异常:", e);
            }
        } finally {
            lock.unlock();
        }
    }
}

class CheckException extends RuntimeException {
    public CheckException(String message) {
        super(message);
    }
}
