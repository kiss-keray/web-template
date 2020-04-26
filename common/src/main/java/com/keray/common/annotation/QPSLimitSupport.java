package com.keray.common.annotation;

import com.keray.common.utils.QpsLimit;

import java.lang.annotation.*;

/**
 * @author by keray
 * 分钟级qps
 * date:2019/10/10 14:30
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QPSLimitSupport {

    String key() default "";

    QpsLimit.Strategy strategy() default QpsLimit.Strategy.wait;

    int waitTime() default QpsLimit.DEFAULT_WAIT_COUNT;

    boolean global() default false;

    int qps();

    int width() default 60;
}
