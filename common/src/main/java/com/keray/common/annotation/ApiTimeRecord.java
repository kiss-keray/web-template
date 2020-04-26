package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/12/4 2:11 PM
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiTimeRecord {

    /**
     * @return 大于该值得毫秒时才统计时间
     */
    int gt() default 2000;

    /**
     * @return 记录接口的名称
     */
    String value() default "";

}
