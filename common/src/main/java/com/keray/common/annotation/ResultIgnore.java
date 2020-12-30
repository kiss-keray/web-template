package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2020/9/18 10:33 下午
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResultIgnore {
}
