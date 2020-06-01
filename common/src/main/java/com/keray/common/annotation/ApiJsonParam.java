package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/9/2 18:06
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiJsonParam {
    boolean value() default true;
}
