package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/9/18 14:05
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataBuriedPointAop {
    String activeType();

    String activeSourceType() default "";

    String activeSourceId() default "";

    String path() default "";

    boolean async() default false;
}
