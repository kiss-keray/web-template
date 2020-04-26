package com.keray.common.annotation;

import com.keray.common.service.ienum.SystemOperationAction;

import java.lang.annotation.*;

/**
 * @author by keray
 * date:2019/8/29 10:30
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemOperationLogAop {
    String tableName() default "-";

    String operationName() default "-";

    SystemOperationAction action() default SystemOperationAction.other;

    boolean async() default false;
}
