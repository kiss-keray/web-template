package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/9/5 16:44
 * kz 延迟表达式
 * dynamicDelay 动态延迟 配合 {@link KScheduleDelay}使用
 * 动态延迟配置优先级高于kz延迟，设置动态延迟后参数化如果为null，
 * 任务会提交后立即执行。但是是以future方式执行，也就是调用方拿不到结果
 * <p>
 * 当kz为空，dynamicDelay=false时，任务会立即执行，以正常调用执行，能拿到方法直接结果
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KSchedule {
    String kz() default "";

    @Deprecated
    String cron() default "";

    String desc();

    String beanName();

    int maxRetry() default 0;

    int retryMillis() default 1000;

    // 设置值动态 dynamicDelay
    boolean dynamicDelay() default false;
}
