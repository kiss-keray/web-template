package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/9/6 14:59
 * kschedule任务动态延迟，毫秒
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KScheduleDelay {
}
