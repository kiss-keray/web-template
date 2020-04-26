package com.keray.common.annotation;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/10/22 14:08
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRecord {

}
